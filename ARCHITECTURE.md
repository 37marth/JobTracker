# JobTracker: 책 실습을 응용하는 아키텍처와 학습 계획

작성일: 2026-09-18

최종 갱신: 2026-09-23

## 1. 목표와 현재 출발점

목표는 Java / Spring Boot 신입 백엔드 취업에 활용할 수 있는 작은 프로젝트를 본인이 직접 만들고, 작성한 CRUD 코드의 역할과 실행 흐름을 설명할 수 있게 되는 것입니다.

출발점은 `SpringPractice`에서 책을 보며 작성한 게시글·댓글 실습입니다. 책 프로젝트를 한 번 마쳤고 다시 보며 떠올리는 입문 단계입니다. 실습 코드가 존재한다는 사실만으로 모든 개념을 이해하거나 백지에서 구현할 수 있다고 가정하지 않습니다.

학습은 기존 코드와 비교 → 필요한 개념 설명 → 본인이 직접 작성 → AI가 저장된 코드 검토 → 실행 결과 확인 순서로 진행합니다. 모르는 용어가 나오면 그 자리에서 설명하고, 새 설정은 왜 필요한지 먼저 설명합니다.

회사 등록·목록 조회 API와 회사명 입력 검증을 AI의 설명과 검토를 받으며 직접 작성했습니다. 회사 등록의 HTTP 201·PostgreSQL 저장은 도구로 확인했고, 본인도 Talend에서 등록·잘못된 회사명의 400·목록 응답을 확인했습니다. 지원내역은 Controller·Service·Entity 생성·Repository 저장까지 코드를 연결했으며 실제 등록 요청과 DB 확인은 다음 단계입니다. 지원내역의 상태·입력 검증·조회·수정·삭제와 관리 화면은 아직 미구현입니다.

## 2. 전체 구조

현재 Java 17, Spring Boot, Spring Data JPA, PostgreSQL, Mustache를 사용합니다. 회사 화면의 JavaScript·fetch 연동은 이후 구현할 계획입니다. 하나의 Spring Boot 애플리케이션과 하나의 DB로 시작합니다.

```text
브라우저의 요청
    → Controller: 요청을 받아 Service 호출
    → Service: 기능 처리 순서와 규칙 담당
    → Repository: DB 저장·조회 요청
    → JPA/Hibernate를 통해 PostgreSQL 접근

조회·처리 결과는 반대 방향으로 돌아가 Controller가 응답합니다.
```

Entity와 DTO는 위 흐름에서 사용하는 데이터 객체입니다. 별도의 실행 단계가 아닙니다.

| 구성 요소 | 역할 | 책 실습에서 비교할 코드 | 현재 코드 또는 계획 |
| --- | --- | --- | --- |
| Entity | DB에 저장할 데이터를 자바 객체로 표현 | `Article` | `Company` |
| Repository | Entity 저장·조회·삭제 | `ArticleRepository` | `CompanyRepository` |
| DTO | 요청 등의 전달 데이터를 담는 객체 | `ArticleForm` | `CompanyRequestDto` |
| Service | DTO 변환, 조회와 저장 등의 기능 처리 | `ArticleService` | `CompanyService` |
| API Controller | HTTP 요청 수신과 응답 | `ArticleApiController` | `CompanyApiController` |
| 화면 Controller | 화면에 필요한 데이터와 템플릿 지정 | `ArticleController` | 현재 `HomeController`, 회사 화면용 `CompanyController`는 계획 |

책 코드는 비교 기준이지 그대로 복사해야 하는 정답은 아닙니다. 오류가 있거나 현재 기능에 맞지 않는 부분은 이유를 설명한 뒤 수정합니다.

화면용 Controller와 API Controller는 같은 Service를 사용할 계획입니다. 등록·수정·삭제 화면의 JavaScript는 `fetch()`로 같은 서버의 API를 호출합니다. 먼저 데이터 저장과 조회를 확인하고 화면을 연결합니다.

### 합의한 사용자 흐름과 첫 완성본

아래는 구현할 화면 계획이며, 현재 화면은 `JobTracker` 제목만 표시합니다.

| 단계 | 사용자 행동 | 화면에 보여줄 내용 |
| --- | --- | --- |
| 1 | 회사 이름·위치를 등록 | 메인 화면의 회사 목록 |
| 2 | 목록에서 회사 상세 보기 선택 | 회사 정보, 그 회사의 지원내역 목록, 지원내역 추가 버튼 |
| 3 | 직무·지원 날짜와 시각·메모를 입력해 저장 | 선택한 회사 아래의 새 지원내역, 초기 상태는 지원완료 |
| 4 | 나중에 결과를 확인하고 기록 수정 | 현재 지원 상태·메모 등 변경 내용 |
| 5 | 잘못 기록한 지원내역 삭제 | 해당 지원내역만 제거, 회사 정보는 유지 |

기록이 없으면 빈 목록 안내를 보여주고 추가할 수 있게 합니다. 같은 회사의 다른 직무에 지원하면 회사 정보를 재등록하지 않고 그 회사 아래에 지원내역을 추가합니다. 지원내역 폼에서는 선택한 회사 정보를 표시하고, 등록 요청 주소의 `{companyId}`로 연결합니다. 상세 화면을 펼치는 방식과 새 페이지로 이동하는 방식 모두 같은 API를 사용할 수 있습니다.

첫 완성본은 이 흐름을 간단한 화면에서 사용할 수 있고, 필수 입력·없는 ID를 처리하며, 주요 동작을 검증하고 요청부터 DB까지 설명할 수 있는 상태입니다. 검색·필터·페이징, 로그인·통계·배포는 그 이후에 검토합니다.

## 3. 현재 데이터와 객체의 역할

현재 `Company`는 책의 `Article` 구조를 응용했습니다.

| 책의 Article | 현재 Company | 의미 |
| --- | --- | --- |
| `Long id` | `Long id` | 행을 구별하는 고유 번호 |
| `String title` | `String name` | 게시글 제목 대신 회사 이름 |
| `String content` | `String location` | 게시글 내용 대신 회사 위치 |

현재 코드의 `@Entity`, `@Id`, `@GeneratedValue`, `@Column`, Lombok 어노테이션을 하나씩 설명하고 이해를 확인합니다. 이미 붙였다는 이유만으로 의미까지 안다고 가정하지 않습니다.

`@Table`은 추가하지 않았으며 기본 이름 규칙에 따라 `Company`는 실제 PostgreSQL의 `company` 테이블에 대응합니다. `id`는 `GenerationType.IDENTITY`로 DB가 생성합니다. `name`과 `location`에는 현재 필수·중복 제약을 추가하지 않았습니다.

`CompanyRequestDto`는 `name`, `location`만 받습니다. `toEntity()`는 `new Company(null, this.name, this.location)`을 반환하며 저장은 수행하지 않습니다. `CompanyRepository`는 `JpaRepository<Company, Long>`을 상속합니다.

`CompanyApiController`와 `CompanyService`는 현재 `@Autowired` 필드 주입을 사용합니다. 컨트롤러는 `companyService.create(...)`를 호출하고, 서비스는 DTO를 Entity로 변환한 뒤 `companyRepository.save(target)`의 결과를 반환합니다. 현재 응답은 `ResponseEntity<Company>`이며 별도 `CompanyResponseDto`는 아직 없습니다.

회사 등록 요청은 DTO의 `name`에 `@NotBlank`, 컨트롤러 매개변수에 `@Valid`를 적용했습니다. 잘못된 회사명은 서비스 호출 전에 기본 400 응답으로 거절합니다. Entity의 DB 컬럼 제약과 요청 DTO의 입력 검증은 별개입니다. 위치 필수 여부와 중복 기준은 아직 미정입니다.

회사 목록은 `indexCompanies()` → `CompanyService.index()` → `findAll()`로 조회하고 `ResponseEntity<List<Company>>`로 200 응답을 반환합니다. 빈 목록은 `[]`입니다.

개발 설정은 `spring.jpa.hibernate.ddl-auto=create`입니다. 서버 시작 시 JPA가 관리하는 테이블을 삭제·재생성하므로 기존 데이터가 사라집니다. 가상 데이터를 사용한 로컬 반복 학습용이며, 요청마다 초기화하는 설정은 아닙니다. 앞서 `update` 설정에서 등록·저장을 검증한 뒤 본인이 `create`로 변경했습니다. 재시작 후 데이터를 유지할 필요가 생기면 로컬 설정을 `update`로 돌립니다.

## 4. 작은 기능 하나를 완성하는 순서

한 단계 안에서도 이해하지 못한 내용이 있으면 더 작게 나눕니다. 파일을 한꺼번에 생성하거나 여러 계층의 완성 코드를 먼저 제공하지 않습니다. 본인이 요청에서 출발할 때 더 이해하기 쉽다고 했으므로, 앞으로는 요청 명세 → Controller → 필요한 Service 처리 → Repository 연결 순서로 생각합니다. 이미 작성한 Entity·DTO·Repository를 활용하며 클래스 작성 순서를 고정하지 않습니다.

| 단계 | 작업 | 현재 상태 |
| --- | --- | --- |
| 1 | `Company`와 `CompanyRepository` 작성 | 작성 완료, `JpaRepository` 사용 |
| 2 | 테이블 생성과 DTO 변환 | `company` 테이블 확인, `CompanyRequestDto.toEntity()` 작성 |
| 3 | 회사 등록 요청과 컨트롤러 작성 | `POST /api/companies`, `@RequestBody`, 201 응답 작성 |
| 4 | 컨트롤러가 요구하는 서비스 메서드 작성 | `CompanyService.create()`에서 변환·저장·반환 연결 |
| 5 | 실제 요청과 저장 확인 | AI 도구로 DB 저장 확인, 본인이 Talend에서 요청·응답 확인 |
| 6 | 회사명 입력 검증 | 작성 완료, 본인이 빈 문자열·공백·null의 400 확인 |
| 7 | 회사 목록 조회 | 작성 완료, 본인이 Talend로 목록 응답 확인 |
| 8 | 지원내역 등록 | 요청 → 회사 조회 → 객체 생성 → 저장 코드 연결. 다음 작업은 실제 요청·DB 확인 |
| 9 | 지원내역 상태·검증·조회·수정·삭제 | 미구현, 한 기능씩 연결하고 확인 |
| 10 | 회사 목록·상세와 지원내역 화면 연결 | 미구현, Mustache와 fetch의 역할부터 학습 |

회사 중복 판단 기준과 처리 방식은 미정입니다. 별도로 기준을 정하되, 회사명 검증 다음에 오류 응답 형식부터 복잡하게 만들지 않고 기본 CRUD 흐름을 우선 연결합니다.

현재 회사 등록 API 명세는 다음과 같습니다. 목록 조회 `GET /api/companies`도 구현했습니다. URL의 `companies`와 DB 테이블 이름 `company`는 서로 같을 필요가 없습니다.

| 항목 | 회사 등록 |
| --- | --- |
| 요청 | `POST /api/companies` |
| Content-Type | `application/json` |
| 요청 본문 예시 | `{"name":"샘플회사","location":"판교"}` |
| 요청 DTO | `CompanyRequestDto` |
| 처리 | DTO를 새 `Company`로 변환하고 Repository로 저장 |
| 성공 응답 | `201 Created`, 본문에 생성된 `id`, `name`, `location` |
| 입력 검증 | 회사명 null·빈 문자열·공백 문자열은 400 응답 |
| 현재 한계 | 중복 내용 등록을 차단하지 않으며 위치 필수 여부 미정 |

본인이 `JpaRepository`가 CRUD 메서드를 제공한다는 책 내용을 떠올려 선택했으므로 현재 구현을 유지합니다. 목록 조회와 페이징 기능은 사용할 때 필요한 메서드를 설명합니다.

요청 DTO는 책의 `ArticleForm`과 비교해 배우되 실제 이름은 `CompanyRequestDto`입니다. 기본 생성자·전체 필드 생성자·getter를 Lombok으로 선언했으며, 실제 JSON 요청을 DTO로 읽어 저장하는 흐름을 확인했습니다.

DTO 변환 위치, Service의 객체 주입 방식, 반환 타입은 실제 메서드 하나를 작성할 때 설명합니다. 처음부터 여러 요청·응답 DTO, 예외 처리 클래스, 생성자 변경을 동시에 요구하지 않습니다. 응답 DTO 분리는 기본 요청·응답 흐름을 확인한 뒤 이유와 함께 추가합니다.

## 5. 지금 진행 중인 목표: 지원내역 CRUD

회사 등록과 조회를 이해한 다음 지원내역으로 확장합니다. 회사 한 곳에 지원내역 여러 건을 연결합니다. 이는 책의 게시글과 댓글 관계를 비교 대상으로 삼아 설명합니다.

현재 `JobApplication`에는 고유 번호 `Long id`, 회사 `Company company`, 직무 `String jobTitle`, 지원 날짜와 시각 `LocalDateTime appliedAt`, 메모 `String memo`가 있습니다. `@ManyToOne`과 `@JoinColumn(name = "company_id")`로 회사를 연결했습니다. 자바에서는 회사 객체를 참조하고 DB에는 회사 ID를 저장하는 관계입니다. 지원 상태 필드는 아직 없습니다.

지원 시각은 사용자가 실제 지원한 시각을 입력하는 값입니다. `LocalDateTime` 자체에는 시간대 정보가 없으며, 개인용으로 한국 현지 시각을 기록합니다. 서버가 기록을 저장한 현재 시각을 자동 입력하는 필드는 아닙니다.

`JobApplicationRepository`는 `JpaRepository<JobApplication, Long>`을 상속합니다. `JobApplicationRequestDto`는 `jobTitle`, `appliedAt`, `memo`를 받습니다. 회사 ID는 주소에서 `@PathVariable`로 받고 요청 본문에는 중복해서 받지 않습니다. 새 지원내역의 ID도 요청 DTO에 없습니다.

등록 요청은 아래 형태로 정했습니다. `JobApplicationApiController`에서 회사 ID와 요청 DTO를 받아 `JobApplicationService.create(...)`에 전달하고, 저장 결과를 201 응답에 담도록 작성했습니다. 실제 요청·응답과 DB 저장 검증은 아직 남아 있습니다.

```http
POST /api/companies/3/job-applications
Content-Type: application/json

{
  "jobTitle": "백엔드 개발자",
  "appliedAt": "2026-09-22T14:30:00",
  "memo": "채용 홈페이지에서 지원"
}
```

주소의 `3`은 예시이며 실제 등록된 회사 ID로 요청합니다. Service에서는 `CompanyRepository.findById(companyId)`로 기존 회사를 조회하고 없으면 `IllegalArgumentException`을 발생시킵니다. 이 예외를 적절한 HTTP 상태와 안내로 바꾸는 처리는 아직 없으며, 현재 404 응답을 보장하는 코드는 아닙니다.

`JobApplication.createJobApplication(company, dto)`는 찾은 회사와 DTO 값으로 새 엔티티를 반환합니다. `@AllArgsConstructor`의 필드 순서에 맞춰 지원내역 ID는 `null`, 회사는 조회한 객체, 나머지는 직무·지원 시각·메모를 전달합니다. 저장은 Service의 `jobApplicationRepository.save(...)`에서 수행합니다. 요청 DTO에 없는 ID를 검사하거나, 본문에 없는 회사 ID를 주소와 비교하는 검사는 필요하지 않습니다.

현재 응답은 `ResponseEntity<JobApplication>`입니다. 요청 처리부터 저장까지 확인한 뒤 응답 DTO로 반환 항목을 따로 정할 계획입니다. 직무·지원 시각의 입력 검증도 아직 추가하지 않았습니다.

| 기능 | 목표 |
| --- | --- |
| 등록(Create) | 회사를 선택해 지원내역 한 건 저장 |
| 조회(Read) | 지원내역 목록과 상세 확인 |
| 수정(Update) | 직무·상태·메모 등의 변경을 DB에 반영 |
| 삭제(Delete) | 선택한 지원내역 삭제, 연결된 회사는 유지 |

지원 상태는 기존 계획대로 지원완료, 서류합격, 면접, 최종합격, 불합격, 지원취소입니다. 첫 등록 시 상태를 지원완료로 설정하고 이후 수정할 계획입니다. 아직 상태 필드나 초기값 처리를 구현하지 않았으며, 기본 등록 연결 후 추가합니다. enum을 사용할 때는 먼저 문자열과 어떤 차이가 있고 왜 필요한지 설명합니다.

## 6. 기본 흐름 이후에 추가할 것

기본 저장·조회가 된다는 것은 중간 학습 목표입니다. 포트폴리오의 완료 기준에는 아래 보완도 포함하지만, 첫 Entity를 작성할 때 모두 요구하지 않습니다.

회사명 필수 검증은 작성하고 요청으로 확인했습니다. 오류 메시지를 직접 구성하는 처리는 보류하고, 지금은 Spring의 기본 400 응답을 사용합니다. 새로운 `Map`, 복잡한 반환 타입, 낯선 검증 결과 메서드를 한꺼번에 도입하지 않습니다. 필요한 오류 안내는 사용자가 이해할 수 있는 크기로 나눠 학습합니다.

`findById(...).orElseThrow(...)`는 조회한 데이터가 없을 때 처리하는 방식이며, 등록 요청의 빈 값 검증과 구분합니다. 지원내역 등록에서 기존 회사 조회에 사용했습니다. 예외 발생과 HTTP 응답 처리는 별개이므로, 없는 회사의 응답은 정상 등록을 확인한 뒤 이어서 학습합니다.

- 회사명·직무 등 필수 입력, 문자열 길이, 공백 입력 검증.
- 존재하지 않는 회사·지원내역 요청에 대한 응답.
- 요청 DTO와 응답 DTO를 나누는 이유와 구현.
- DB 작업의 트랜잭션: 여러 작업을 함께 확정하거나 되돌려야 하는 이유.
- 정상 저장, 잘못된 입력, 없는 ID, 수정·삭제 결과를 확인하는 테스트.
- 회사명 검색, 지원 상태 필터, 페이징은 기본 CRUD와 화면을 완성한 뒤의 확장 과제로 둡니다.

초기의 상세 설계에 있던 별도 화면/API 예외 처리기, 고정된 컬럼 길이, 모든 DTO 클래스명, 세부 HTTP 응답 형식은 이 단계에서 다시 검토합니다. 학습 전에 외워야 할 규칙으로 취급하지 않습니다.

개인의 로컬 사용으로 시작합니다. 로그인, 회사 수정·삭제, 상태 변경 이력, 통계, 면접 일정, 배포는 기본 기능 완료 후 검토합니다.

## 7. 코드 검토와 기록 원칙

- AI는 본인이 작성하고 저장한 프로젝트 파일과 설정을 직접 확인합니다. 확인 가능한 코드를 매번 채팅에 복사하도록 요구하지 않습니다.
- 검토에서는 맞게 응용한 부분, 실제 오류, 나중에 개선할 부분을 구분합니다. 새로운 설계 취향을 필수 수정사항처럼 제시하지 않습니다.
- 새 문법·어노테이션·기술은 목적과 필요성을 설명한 뒤 도입합니다.
- 새 개념은 하나씩 도입합니다. 이미 이해한 내용을 무조건 처음부터 반복하지 않고, 익숙한 책 코드와 이번에 달라지는 부분을 연결해서 설명합니다.
- 기능 코드는 본인이 직접 작성하고, AI는 요청에 맞춰 설명·힌트·최소 수정·검토를 제공합니다.
- AI가 수행한 환경 설정이나 검증을 본인이 독립적으로 구현한 경험으로 기록하지 않습니다.
- 각 학습 후 직접 작성한 부분, 이해한 내용, 남은 질문, 다음 시작점을 남깁니다.
- 커밋 메시지는 한국어로 쓰고 환경 재설정과 새 기능 구현을 구분합니다.
