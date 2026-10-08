# JobTracker 아키텍처

## 구성

하나의 Spring Boot 애플리케이션과 PostgreSQL을 사용합니다.

```text
HTTP 요청 → Controller → Service → Repository → JPA/Hibernate → PostgreSQL
HTTP 응답 ← Controller ← Service ← 조회·저장 결과
```

| 구성 | 역할 | 클래스 |
| --- | --- | --- |
| Controller | 요청을 받아 서비스를 호출하고 JSON 또는 화면 반환 | CompanyApiController, JobApplicationApiController, HomeController |
| Service | 회사·지원내역 조회·등록·수정·삭제 처리, 응답 DTO 변환 | CompanyService, JobApplicationService |
| Repository | 엔티티 저장·조회·삭제 | CompanyRepository, JobApplicationRepository |
| Entity | 저장할 데이터와 연관관계 표현 | Company, JobApplication |
| 요청 DTO | 요청 본문의 입력값 전달 | CompanyRequestDto, JobApplicationRequestDto, JobApplicationStatusRequestDto |
| 응답 DTO | 회사·지원내역에서 응답에 필요한 값 전달 | CompanyResponseDto, JobApplicationResponseDto |
| 화면 DTO | 회사 정보와 해당 회사의 지원내역 목록을 묶어 템플릿에 전달 | CompanyViewDto |

응답 DTO 변환은 Service에서 처리하고, Controller는 반환된 DTO에 HTTP 상태를 붙여 응답합니다. 회사와 지원내역의 등록·조회·수정에 적용했습니다. Repository는 계속 엔티티를 조회·저장합니다.

`HomeController`는 `CompanyService.index()`로 회사 DTO 목록을 조회합니다. 각 회사의 ID로 `JobApplicationService.index(companyId)`를 호출하고, 회사 정보와 지원내역 목록을 `CompanyViewDto`에 묶어 `Model`의 `companies`에 담습니다. 템플릿은 `{{#companies}}` 안의 `{{#applications}}`로 회사별 지원내역을 반복 출력합니다. 회사 목록과 지원내역 모두 서버의 Mustache가 HTML로 만들며, HTML의 `<details>`·`<summary>`로 회사 이름을 눌러 펼치고 접습니다. 클릭 시 JavaScript로 지원내역을 조회하거나 HTML 요소를 만들지 않습니다.

`CompanyViewDto`는 화면 전달용이며, API는 기존 `CompanyResponseDto`와 `JobApplicationResponseDto`를 사용합니다. `JobApplicationStatus.getDisplayName()`은 한글 이름을 반환하는 메서드이며, 현재 상태 드롭다운은 HTML에 enum 이름과 한글 선택지를 직접 작성합니다. API와 DB에는 enum 이름을 사용합니다. 회사와 지원내역 등록 버튼은 `fetch()`로 기존 등록 API를 호출한 뒤 201 응답이면 페이지를 새로고침합니다.

지원내역 입력칸과 버튼에는 회사 ID를 포함한 HTML ID와 `data-company-id`를 사용합니다. 클릭한 버튼의 회사 ID로 해당 회사의 입력값을 읽어 `POST /api/companies/{companyId}/job-applications`에 보냅니다. 등록 성공 시 주소 끝에 `#company-회사ID`를 기록하며, 페이지를 다시 불러오면 그 ID의 `<details>`를 펼쳐 새 지원내역을 표시합니다. 응답 JSON으로 목록 HTML을 직접 만드는 방식은 사용하지 않습니다.

현재 첫 화면은 회사 전체와 각 회사의 지원내역을 미리 조회합니다. 회사 수만큼 지원내역 조회가 추가되므로, 목록이 커지면 조회 방식과 페이징을 검토합니다. 펼치기 자체는 서버 요청을 보내지 않고, 최신 데이터는 페이지 새로고침 시 반영됩니다. 빈 회사 목록 안내도 템플릿에 작성되어 있으며, 빈 회사 목록 화면의 실행 확인은 아직입니다.

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
| JobApplication | status | JobApplicationStatus | 현재 지원 상태. enum 이름을 문자열로 저장 |

두 엔티티의 ID는 `GenerationType.IDENTITY`를 사용합니다. `appliedAt`은 사용자가 실제 지원한 날짜를 입력하며, 시각과 시간대 정보는 저장하지 않습니다. `HomeController`가 `LocalDate.now()`를 `Model`의 `today`로 전달하고, 날짜 입력칸의 `value="{{today}}"`로 오늘 날짜를 기본 표시합니다. 사용자는 날짜를 변경할 수 있으며, API에는 입력칸에서 선택한 날짜를 직접 보냅니다. 날짜 필수 검증은 유지합니다.

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
3. 회사가 없으면 `ResponseStatusException(HttpStatus.NOT_FOUND, ...)`을 발생시켜 저장을 중단하고, Spring이 404로 응답합니다.
4. `JobApplication.createJobApplication(company, dto)`로 지원내역 객체를 만듭니다.
5. `JobApplicationRepository.save()`로 저장한 뒤 `JobApplicationResponseDto.createJobApplicationDto()`로 변환해 201 응답 본문에 담습니다.

요청 DTO에는 지원내역 ID와 회사 ID가 없습니다. 회사는 주소의 ID로 조회한 객체를 사용하고, 새 지원내역의 ID는 null로 생성한 뒤 DB에서 부여받습니다.

지원내역 응답 DTO는 `id`, `companyId`, `jobTitle`, `appliedAt`, `memo`, `status`를 담습니다. `getCompany().getId()`로 연결된 회사의 ID를 가져오며 회사 객체 전체는 응답에 포함하지 않습니다. 새 지원내역은 생성 메서드에서 `APPLIED` 상태로 설정합니다.

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

### 지원 상태 변경

`PATCH /api/job-applications/{id}/status`에서 `JobApplicationStatusRequestDto`로 상태만 받습니다. `@Valid`와 `@NotNull`로 누락·null을 검사하고, enum에 없는 문자열은 요청을 DTO로 바꾸는 과정에서 거절합니다.

Service가 지원내역을 찾고 `changeStatus()`로 상태만 바꾼 뒤 `save()`합니다. 결과는 응답 DTO로 변환해 200으로 반환하며, 대상이 없으면 Controller가 404를 반환합니다. 직무·날짜·메모는 유지하고, 기존 `patch()`도 상태를 변경하지 않습니다. `@Enumerated(EnumType.STRING)`으로 DB에 enum 이름을 저장합니다.

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

API 테스트 요청은 `http/` 아래에서 회사 CRUD, 지원내역 CRUD, 입력값 검증, 회사 연쇄 삭제, 없는 대상 처리, 지원 상태 변경으로 나눴습니다. 각 파일은 준비부터 결과 확인, 데이터 정리, 정리 후 GET 확인까지 순서대로 실행하며, 등록 응답의 ID를 자동으로 다음 요청에 전달합니다. 없는 대상 처리 그룹에는 삭제한 회사에 지원내역을 등록할 때의 404 확인과 이후 지원내역·회사 목록 조회도 포함합니다. 상태 변경 그룹은 기본 상태, 변경 후 조회, 다른 정보 유지, 잘못된 입력과 없는 대상을 검사합니다. 파일 목록과 사용법은 README의 API 테스트 항목에 정리했습니다.

등록·수정 요청에서 회사명과 지원 직무는 `@NotBlank`, 지원 날짜는 `@NotNull`로 검사합니다. Controller의 `@Valid`가 검증을 실행하며, 실패하면 Spring의 기본 400 응답을 사용합니다. 회사 위치와 지원 메모는 선택사항입니다.

수정·삭제 대상이 없으면 회사·지원내역 모두 404로 응답하는 것을 확인했습니다. 직무의 빈 문자열·공백·null, 날짜만 null인 등록·수정 요청은 400으로 거절되며 새 내역이 생기거나 기존 내역이 바뀌지 않았습니다. 메모 null은 등록할 수 있습니다. Java 17 컴파일과 API 확인을 마쳤으며, JUnit 테스트는 아직 실행하지 않았습니다.

지원내역 등록에서 존재하지 않는 회사를 조회하면 `ResponseStatusException`에 `HttpStatus.NOT_FOUND`를 지정해 404로 응답합니다. 예외가 발생하면 지원내역 생성·저장으로 진행하지 않습니다. API로 404 응답과 이후 빈 지원내역 목록, 기존 회사·지원내역 유지를 확인했습니다.

## 화면과 후속 기능

첫 화면에는 회사 이름과 위치가 표시되며, 회사 이름을 누르면 같은 화면에서 해당 회사의 지원내역 전체가 펼쳐집니다. 직무·지원 날짜·메모와 현재 상태를 Mustache로 전달하고, ‘지원내역’ 제목과 첫 내역 사이에 구분선을 넣었습니다. 회사별 지원내역 등록 입력칸과 버튼을 API에 연결했으며, 등록 후 해당 회사가 펼쳐진 상태로 새 내역을 표시합니다. 상태 드롭다운은 `data-status` 값으로 초기 선택을 설정하고, `change` 이벤트에서 상태 변경 API를 호출합니다. 200 응답이면 같은 회사의 주소 위치를 기록하고 새로고침하며, 오류 상태 응답이면 선택칸을 기존 상태로 되돌립니다. 화면 초안과 개선사항은 [목업 안내](docs/mockups/README.md)에 보관했습니다.

지원 상태 API와 선택 화면은 다음 기준으로 구현했습니다.

- 첫 등록은 `지원완료`로 시작합니다.
- 상태는 `지원완료`, `서류합격`, `면접`, `최종합격`, `불합격`, `지원취소`를 사용합니다.
- 현재 상태만 저장하며 변경 이력은 별도로 저장하지 않습니다.

회사 연쇄 삭제와 없는 회사 등록 요청의 404 처리를 확인했고, 지원 상태 변경은 `06-status.http`를 수동 실행해 확인했습니다. 회사와 지원내역 등록, 상태 선택 화면은 기존 API에 연결했습니다. 회사별 지원내역 출력은 실제 API 응답 사본과 추가 예시 데이터를 사용하는 별도 검증 서버에서 실제 컨트롤러·Mustache로 확인했으며, 이후 실제 화면에서 사용자 수동 확인을 마쳤습니다. 2026-10-08 중간점검에서 Java 17 빌드, 현재 서버의 첫 화면 200과 오늘 날짜 기본값을 확인했습니다. 상태 선택 화면의 사용자 확인 후 별도 테스트 데이터로 면접·최종합격 상태 변경과 재조회, 새 HTML의 상태 값 일치, 다른 정보 유지를 교차검증했습니다. 다음은 회사 이름·위치 수정 화면이며, 이후 지원내역 수정·삭제와 회사 삭제 확인창을 구현합니다. 검색·필터·페이징 등의 확장 기능은 기본 기능과 화면을 완성한 뒤 검토합니다.

## 개발 DB 설정

현재 `ddl-auto=create`는 서버 시작 시 JPA가 관리하는 테이블을 삭제·재생성합니다. `spring.jpa.defer-datasource-initialization=true`와 `spring.sql.init.mode=always` 설정으로 테이블 생성 후 `data.sql`이 테스트 회사 두 개를 등록합니다. 로컬 개발용 DB에서 사용하며, 데이터 유지가 필요하면 `update`로 변경하고 SQL 초기화는 `never`로 끕니다.

접속 비밀번호는 소스에 저장하지 않고 환경변수 `DB_PASSWORD`로 전달합니다. 구체적인 실행 절차와 요청 예시는 [README](README.md)를 참고합니다.
