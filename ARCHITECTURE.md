# JobTracker 아키텍처

## 구성

하나의 Spring Boot 애플리케이션과 PostgreSQL을 사용합니다.

```text
HTTP 요청 → Controller → Service → Repository → JPA/Hibernate → PostgreSQL
HTTP 응답 ← Controller ← Service ← 조회·저장 결과
```

| 구성 | 역할 | 클래스 |
| --- | --- | --- |
| Controller | 요청을 받아 서비스를 호출하고 HTTP 응답 반환 | CompanyApiController, JobApplicationApiController |
| Service | 회사·지원내역 조회, 생성·수정·저장 순서 처리 | CompanyService, JobApplicationService |
| Repository | 엔티티 저장·조회 | CompanyRepository, JobApplicationRepository |
| Entity | 저장할 데이터와 연관관계 표현 | Company, JobApplication |
| 요청 DTO | 요청 본문의 입력값 전달 | CompanyRequestDto, JobApplicationRequestDto |

`HomeController`는 `home/home.mustache`를 반환합니다. 회사·지원내역 화면은 미구현이며, Mustache와 JavaScript의 `fetch()`로 기존 API에 연결할 계획입니다.

## 데이터 관계

회사 한 곳에 지원내역 여러 건을 연결합니다. 현재 `JobApplication`이 `Company`를 참조하는 단방향 다대일 관계이며, `@JoinColumn(name = "company_id")`로 회사 ID를 저장합니다.

| 엔티티 | 필드 | 타입 | 의미 |
| --- | --- | --- | --- |
| Company | id | Long | DB가 생성하는 회사 ID |
| Company | name | String | 회사명 |
| Company | location | String | 회사 위치 |
| JobApplication | id | Long | DB가 생성하는 지원내역 ID |
| JobApplication | company | Company | 지원한 회사 |
| JobApplication | jobTitle | String | 지원한 직무 |
| JobApplication | appliedAt | LocalDate | 실제 지원한 날짜 |
| JobApplication | memo | String | 메모 |

두 엔티티의 ID는 `GenerationType.IDENTITY`를 사용합니다. `appliedAt`은 사용자가 실제 지원한 날짜를 입력하며, 시각과 시간대 정보는 저장하지 않습니다.

## 요청 처리

### 회사 등록·조회

- 등록: 요청 JSON → `CompanyRequestDto` → `@Valid` 검사 → `toEntity()`로 회사 객체 생성 → `save()` → 201 응답.
- 목록: `CompanyService.index()` → `findAll()` → 회사 목록을 200 응답으로 반환.

회사명은 DTO의 `@NotBlank`로 검사하며, null·빈 문자열·공백만 있는 문자열을 400으로 거절합니다. 위치 필수 여부와 중복 판단 기준은 아직 정하지 않았습니다.

### 지원내역 등록

요청 주소는 `POST /api/companies/{companyId}/job-applications`입니다.

1. Controller가 주소에서 `companyId`, 본문에서 `jobTitle`·`appliedAt`·`memo`를 받습니다.
2. Service가 `CompanyRepository.findById(companyId)`로 기존 회사를 조회합니다.
3. 회사가 없으면 `IllegalArgumentException`을 발생시켜 저장을 중단합니다.
4. `JobApplication.createJobApplication(company, dto)`로 지원내역 객체를 만듭니다.
5. `JobApplicationRepository.save()`의 결과를 201 응답 본문에 담습니다.

요청 DTO에는 지원내역 ID와 회사 ID가 없습니다. 회사는 주소의 ID로 조회한 객체를 사용하고, 새 지원내역의 ID는 null로 생성한 뒤 DB에서 부여받습니다.

현재 응답에는 엔티티를 직접 사용합니다. 지원내역 등록·조회와 수정 후 재조회를 확인했습니다.

### 회사별 지원내역 조회

`GET /api/companies/{companyId}/job-applications`는 서비스의 `index(companyId)`를 거쳐 `findByCompanyId(companyId)`를 호출합니다. Spring Data JPA가 메서드 이름의 `CompanyId`를 `JobApplication.company.id`로 해석해 해당 회사의 지원내역만 조회합니다. 회사 존재 여부는 별도로 검사하지 않아, 없는 회사 ID도 빈 목록을 반환합니다.

### 회사·지원내역 수정

회사 수정은 `PATCH /api/companies/{id}`, 지원내역 수정은 `PATCH /api/job-applications/{id}`를 사용합니다.

1. Controller가 주소의 대상 ID와 검증한 요청 DTO를 Service에 전달합니다.
2. Service가 `findById(id)`로 기존 엔티티를 찾습니다.
3. 대상이 있으면 엔티티의 `patch(dto)`로 값을 바꾸고 `save()`합니다.
4. 대상이 없으면 null을 반환하고, Controller가 본문 없는 404로 응답합니다.
5. 수정에 성공하면 변경된 엔티티를 200 응답에 담습니다.

회사는 이름·위치, 지원내역은 직무·날짜·메모를 변경합니다. 수정할 때 기존 ID와 회사 연결은 유지합니다. 수정 요청 DTO는 등록용과 같으며, 생략한 값을 자동으로 기존 값으로 유지하지 않습니다. 선택 필드인 위치·메모는 생략하거나 null로 보내면 비워집니다.

## 검증과 오류 처리

등록·수정 요청에서 회사명과 지원 직무는 `@NotBlank`, 지원 날짜는 `@NotNull`로 검사합니다. Controller의 `@Valid`가 검증을 실행하며, 실패하면 Spring의 기본 400 응답을 사용합니다. 회사 위치와 지원 메모는 선택사항입니다. 응답 DTO는 아직 없습니다.

수정 대상이 없으면 404로 응답합니다. 지원내역 등록에서 존재하지 않는 회사를 조회했을 때는 `IllegalArgumentException`을 발생시키지만, 이를 404 등으로 변환하는 별도 오류 처리는 아직 없습니다.

## 화면과 후속 기능

회사 목록에서 회사를 선택하면 회사 정보와 지원내역을 보여주고, 해당 회사에 지원내역을 추가하도록 구성합니다. 화면을 펼칠지 새 페이지로 이동할지와 관계없이 같은 API를 사용합니다.

지원 상태는 아직 미구현이며 다음 기준으로 추가할 계획입니다.

- 첫 등록은 `지원완료`로 시작합니다.
- 상태는 `지원완료`, `서류합격`, `면접`, `최종합격`, `불합격`, `지원취소`를 사용합니다.
- 현재 상태만 저장하며 변경 이력은 별도로 저장하지 않습니다.
- 지원내역을 삭제해도 연결된 회사는 유지합니다.

회사·지원내역 삭제와 관리 화면을 완성한 뒤 검색·필터·페이징 등의 확장 기능을 검토합니다.

## 개발 DB 설정

현재 `ddl-auto=create`는 서버 시작 시 JPA가 관리하는 테이블을 삭제·재생성합니다. `spring.jpa.defer-datasource-initialization=true`와 `spring.sql.init.mode=always` 설정으로 테이블 생성 후 `data.sql`이 테스트 회사 두 개를 등록합니다. 로컬 개발용 DB에서 사용하며, 데이터 유지가 필요하면 `update`로 변경하고 SQL 초기화는 `never`로 끕니다.

접속 비밀번호는 소스에 저장하지 않고 환경변수 `DB_PASSWORD`로 전달합니다. 구체적인 실행 절차와 요청 예시는 [README](README.md)를 참고합니다.
