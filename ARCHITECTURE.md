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
| Service | 회사·지원내역 조회·등록·수정·삭제 처리, 응답 DTO 변환 | CompanyService, JobApplicationService |
| Repository | 엔티티 저장·조회·삭제 | CompanyRepository, JobApplicationRepository |
| Entity | 저장할 데이터와 연관관계 표현 | Company, JobApplication |
| 요청 DTO | 요청 본문의 입력값 전달 | CompanyRequestDto, JobApplicationRequestDto |
| 응답 DTO | 회사·지원내역에서 응답에 필요한 값 전달 | CompanyResponseDto, JobApplicationResponseDto |

응답 DTO 변환은 Service에서 처리하고, Controller는 반환된 DTO에 HTTP 상태를 붙여 응답합니다. 회사와 지원내역의 등록·조회·수정에 적용했습니다. Repository는 계속 엔티티를 조회·저장합니다.

`HomeController`는 `home/home.mustache`를 반환합니다. 회사·지원내역 화면은 미구현이며, Mustache와 JavaScript의 `fetch()`로 기존 API에 연결할 계획입니다.

## 데이터 관계

회사 한 곳에 지원내역 여러 건을 연결합니다. `JobApplication.company`는 `@ManyToOne`과 `@JoinColumn(name = "company_id")`로 회사 ID를 저장하고, `Company.jobApplications`는 `@OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)`로 같은 관계를 목록으로 표현합니다. `mappedBy`에는 지원내역 엔티티의 필드명인 `company`를 사용합니다.

| 엔티티 | 필드 | 타입 | 의미 |
| --- | --- | --- | --- |
| Company | id | Long | DB가 생성하는 회사 ID |
| Company | name | String | 회사명 |
| Company | location | String | 회사 위치 |
| Company | jobApplications | List<JobApplication> | 이 회사에 연결된 지원내역 목록 |
| JobApplication | id | Long | DB가 생성하는 지원내역 ID |
| JobApplication | company | Company | 지원한 회사 |
| JobApplication | jobTitle | String | 지원한 직무 |
| JobApplication | appliedAt | LocalDate | 실제 지원한 날짜 |
| JobApplication | memo | String | 메모 |

두 엔티티의 ID는 `GenerationType.IDENTITY`를 사용합니다. `appliedAt`은 사용자가 실제 지원한 날짜를 입력하며, 시각과 시간대 정보는 저장하지 않습니다. 화면에서는 오늘 날짜를 기본으로 표시하고 과거 날짜로 변경할 수 있게 할 예정이며, 현재 API에는 날짜를 직접 보냅니다.

회사 객체의 지원내역 목록은 빈 목록으로 초기화합니다. 기존 회사 생성 코드를 유지하기 위해 ID·이름·위치를 받는 생성자를 직접 작성했습니다. `@ToString.Exclude`로 회사의 문자열 출력에서 지원내역 목록을 제외합니다.

## 요청 처리

### 회사 등록·조회

- 등록: 요청 JSON → `CompanyRequestDto` → `@Valid` 검사 → Service에서 `toEntity()`로 회사 객체 생성 → `save()` → `CompanyResponseDto.createCompanyDto()`로 변환 → Controller에서 201 응답.
- 목록: `CompanyService.index()` → `findAll()` → `stream().map(...).collect(Collectors.toList())`로 각 회사를 응답 DTO로 변환 → Controller에서 DTO 목록을 200 응답으로 반환.

회사명은 DTO의 `@NotBlank`로 검사하며, null·빈 문자열·공백만 있는 문자열을 400으로 거절합니다. 현재 위치는 선택사항이며, 회사 중복 판단 기준은 아직 정하지 않았습니다.

### 지원내역 등록

요청 주소는 `POST /api/companies/{companyId}/job-applications`입니다.

1. Controller가 주소에서 `companyId`, 본문에서 `jobTitle`·`appliedAt`·`memo`를 받습니다.
2. Service가 `CompanyRepository.findById(companyId)`로 기존 회사를 조회합니다.
3. 회사가 없으면 `IllegalArgumentException`을 발생시켜 저장을 중단합니다.
4. `JobApplication.createJobApplication(company, dto)`로 지원내역 객체를 만듭니다.
5. `JobApplicationRepository.save()`로 저장한 뒤 `JobApplicationResponseDto.createJobApplicationDto()`로 변환해 201 응답 본문에 담습니다.

요청 DTO에는 지원내역 ID와 회사 ID가 없습니다. 회사는 주소의 ID로 조회한 객체를 사용하고, 새 지원내역의 ID는 null로 생성한 뒤 DB에서 부여받습니다.

지원내역 응답 DTO는 `id`, `companyId`, `jobTitle`, `appliedAt`, `memo`를 담습니다. `getCompany().getId()`로 연결된 회사의 ID를 가져오며 회사 객체 전체는 응답에 포함하지 않습니다. DTO 적용 후 등록·조회·수정과 수정 후 재조회를 확인했습니다.

### 회사별 지원내역 조회

`GET /api/companies/{companyId}/job-applications`는 서비스의 `index(companyId)`를 거쳐 `findByCompanyId(companyId)`를 호출합니다. Spring Data JPA가 메서드 이름의 `CompanyId`를 `JobApplication.company.id`로 해석해 해당 회사의 지원내역만 조회합니다. 회사 존재 여부는 별도로 검사하지 않아, 없는 회사 ID도 빈 목록을 반환합니다.

조회한 엔티티 목록은 서비스에서 `stream().map(...).collect(Collectors.toList())`로 응답 DTO 목록으로 변환합니다.

### 회사·지원내역 수정

회사 수정은 `PATCH /api/companies/{id}`, 지원내역 수정은 `PATCH /api/job-applications/{id}`를 사용합니다.

1. Controller가 주소의 대상 ID와 검증한 요청 DTO를 Service에 전달합니다.
2. Service가 `findById(id)`로 기존 엔티티를 찾습니다.
3. 대상이 있으면 엔티티의 `patch(dto)`로 값을 바꾸고 `save()`합니다.
4. 대상이 없으면 null을 반환하고, Controller가 본문 없는 404로 응답합니다.
5. Service에서 회사를 `CompanyResponseDto`, 지원내역을 `JobApplicationResponseDto`로 변환합니다. Controller는 해당 결과를 200 응답에 담습니다.

회사는 이름·위치, 지원내역은 직무·날짜·메모를 변경합니다. 수정할 때 기존 ID와 회사 연결은 유지합니다. 수정 요청 DTO는 등록용과 같으며, 생략한 값을 자동으로 기존 값으로 유지하지 않습니다. 선택 필드인 위치·메모는 생략하거나 null로 보내면 비워집니다.

### 회사·지원내역 삭제

요청 주소는 `DELETE /api/companies/{id}`, `DELETE /api/job-applications/{id}`입니다.

1. Service가 `findById(id)`로 대상을 찾습니다.
2. 대상이 있으면 `delete(target)`으로 삭제하고, 대상 객체를 Controller에 반환합니다.
3. 대상이 없으면 null을 반환합니다.
4. Controller는 null이면 404, 삭제에 성공하면 204를 응답합니다. 응답 본문이 없으므로 반환형은 `ResponseEntity<Void>`입니다.

삭제 결과로 반환하는 엔티티는 Controller가 대상 존재 여부를 판단하는 데 사용하며 응답 본문에 담지 않습니다. 따라서 삭제에는 응답 DTO 변환이 필요하지 않습니다.

`Company.jobApplications`의 `CascadeType.REMOVE` 설정에 따라, 기존 `companyRepository.delete(target)` 호출로 해당 회사의 지원내역을 먼저 삭제한 뒤 회사를 삭제합니다. JPA를 통한 삭제에 적용되며, DB 외래키에 `ON DELETE CASCADE`를 설정한 것은 아닙니다.

삭제 정책과 확인 결과는 다음과 같습니다.

- 회사 삭제는 그 회사의 지원내역도 함께 삭제합니다.
- 지원내역 하나를 삭제할 때는 회사와 다른 지원내역을 유지합니다.
- JPA cascade는 `Company`에서 `JobApplication`으로만 삭제를 전달합니다. 지원내역의 `company` 필드에는 삭제 전파를 설정하지 않습니다.
- 회사와 연결된 지원내역 두 건의 삭제, 다른 회사와 그 지원내역의 유지, 지원내역이 없는 회사 삭제를 API로 확인했습니다.
- 회사·지원내역 삭제의 204와 빈 본문, 같은 대상을 다시 삭제할 때의 404를 확인했습니다.

화면의 삭제 확인창은 아직 미구현입니다. "이 회사를 삭제하면 등록된 지원내역도 모두 삭제됩니다. 정말 삭제하시겠습니까?"를 표시하고, 취소하면 요청을 보내지 않으며 삭제를 선택했을 때만 DELETE를 보내도록 할 예정입니다. API를 직접 호출하면 이 확인 절차는 없습니다.

## 검증과 오류 처리

등록·수정 요청에서 회사명과 지원 직무는 `@NotBlank`, 지원 날짜는 `@NotNull`로 검사합니다. Controller의 `@Valid`가 검증을 실행하며, 실패하면 Spring의 기본 400 응답을 사용합니다. 회사 위치와 지원 메모는 선택사항입니다.

수정·삭제 대상이 없으면 회사·지원내역 모두 404로 응답하는 것을 확인했습니다. 직무의 빈 문자열·공백·null, 날짜만 null인 등록·수정 요청은 400으로 거절되며 새 내역이 생기거나 기존 내역이 바뀌지 않았습니다. 메모 null은 등록할 수 있습니다. Java 17 컴파일과 API 확인을 마쳤으며, JUnit 테스트는 아직 실행하지 않았습니다.

지원내역 등록에서 존재하지 않는 회사를 조회하면 `IllegalArgumentException`이 발생합니다. 이를 HTTP 상태로 변환하는 별도 처리가 없어 현재 500을 반환하는 것을 확인했습니다. 이 경우를 404로 처리하는 작업이 다음 순서입니다.

## 화면과 후속 기능

회사 목록에서 회사를 선택하면 회사 정보와 지원내역을 보여주고, 해당 회사에 지원내역을 추가하도록 구성합니다. 화면을 펼칠지 새 페이지로 이동할지와 관계없이 같은 API를 사용합니다.

지원 상태는 아직 미구현이며 다음 기준으로 추가할 계획입니다.

- 첫 등록은 `지원완료`로 시작합니다.
- 상태는 `지원완료`, `서류합격`, `면접`, `최종합격`, `불합격`, `지원취소`를 사용합니다.
- 현재 상태만 저장하며 변경 이력은 별도로 저장하지 않습니다.

회사 연쇄 삭제 구현·검증을 마쳤습니다. 다음은 없는 회사에 지원내역을 등록할 때의 404 응답 처리입니다. 이후 지원 상태와 관리 화면, 회사 삭제 확인창을 구현합니다. 검색·필터·페이징 등의 확장 기능은 기본 기능과 화면을 완성한 뒤 검토합니다.

## 개발 DB 설정

현재 `ddl-auto=create`는 서버 시작 시 JPA가 관리하는 테이블을 삭제·재생성합니다. `spring.jpa.defer-datasource-initialization=true`와 `spring.sql.init.mode=always` 설정으로 테이블 생성 후 `data.sql`이 테스트 회사 두 개를 등록합니다. 로컬 개발용 DB에서 사용하며, 데이터 유지가 필요하면 `update`로 변경하고 SQL 초기화는 `never`로 끕니다.

접속 비밀번호는 소스에 저장하지 않고 환경변수 `DB_PASSWORD`로 전달합니다. 구체적인 실행 절차와 요청 예시는 [README](README.md)를 참고합니다.
