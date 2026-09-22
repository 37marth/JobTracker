# JobTracker

**회사별 취업 지원내역과 진행 상태를 한곳에서 관리하기 위해 개발 중인 Spring Boot 웹 프로젝트입니다.**

지원한 회사, 직무, 지원일, 현재 결과와 메모를 기록하고 회사명과 상태로 필요한 지원내역을 찾아보는 것을 목표로 합니다.

> 회사 등록·목록 조회 API와 회사명 입력 검증을 작성하고 직접 요청으로 확인했습니다. 지원내역도 요청 받기 → 회사 찾기 → 지원 기록 만들기 → 저장하기까지 코드를 연결했습니다. 지원내역 등록 요청과 실제 DB 저장 확인은 다음에 진행합니다. 회사·지원내역 관리 화면은 아직 구현하지 않았습니다.

최종 갱신: 2026-09-23

## 프로젝트 목표

다음과 같은 질문을 기록을 통해 확인할 수 있도록 만드는 것이 서비스의 목표입니다.

- 어느 회사의 어떤 직무에 언제 지원했는가?
- 각 지원의 현재 상태는 무엇인가?
- 같은 회사에 지원한 다른 내역이 있는가?
- 지원할 때 남긴 메모는 무엇인가?

개발 과정에서는 Spring MVC와 REST API의 요청 처리, JPA를 이용한 데이터 저장, 입력 검증과 오류 처리, 테스트를 통한 동작 확인을 익히고 적용합니다.

## 사용자가 이용할 화면 흐름

2026-09-23에 합의한 화면 계획입니다. 아래 화면과 버튼은 앞으로 구현할 대상이며, 현재 메인 화면에는 `JobTracker` 제목만 표시됩니다.

1. **회사 등록과 메인 목록**: 회사 이름과 위치를 입력해 등록하고, 메인 화면에서 회사 목록을 확인합니다.
2. **회사 상세 보기**: 목록에서 회사를 선택하면 회사 정보와 그 회사에 연결된 지원내역이 보입니다. 기록이 없으면 비어 있다는 안내와 `지원내역 추가` 버튼을 표시합니다.
3. **지원내역 추가**: 선택한 회사에 지원한 직무, 지원 날짜·시각, 메모를 입력합니다. 회사 이름과 위치를 다시 입력하지 않고 선택한 회사의 ID로 연결합니다.
4. **지원 결과 관리**: 저장한 기록을 다시 조회하고, 결과에 따라 상태·메모 등을 수정하거나 잘못 등록한 지원내역을 삭제합니다. 지원내역을 삭제해도 회사는 유지합니다.

```text
회사 등록 → 메인 회사 목록 → 회사 상세
                              ├─ 지원내역 추가
                              └─ 지원내역 조회 → 수정 또는 삭제
```

회사 한 곳에 다른 직무로 다시 지원했다면 기존 회사 아래에 지원내역을 추가합니다. 첫 저장 시 상태는 `지원완료`로 시작하고, 이후 서류합격·면접 등 현재 상태로 변경하도록 만들 계획입니다. 상태 필드와 이 초기값 처리는 아직 구현하지 않았습니다.

## 현재 구현 상태

| 항목 | 상태 |
| --- | --- |
| Spring Boot 프로젝트 생성 및 서버 실행 | 초기 서버 실행 확인 |
| 첫 화면 | `GET /` 요청에 `JobTracker` 제목 표시, HTTP 200 응답 확인 |
| PostgreSQL 준비 | 개발용 `jobtracker_db` 생성, 드라이버와 접속 설정 추가 |
| 애플리케이션의 DB 연결 | 노트북에서 진행했던 연결을 2026-09-17 새 컴퓨터에서 재설정하고 PostgreSQL 연결 및 JPA 초기화 재확인 |
| 회사 등록 | `POST /api/companies` 구현, HTTP 201·생성된 ID·DB 저장 확인 |
| 회사명 입력 검증 | `@NotBlank`·`@Valid` 적용. 본인이 정상 등록 결과와 빈 문자열·공백·`null`의 400 응답 확인 |
| 회사 목록 조회 | `GET /api/companies` 구현. 본인이 등록 후 목록 응답 확인 |
| 중복 처리 | 미구현. 회사 중복 판단 기준은 별도 결정 필요 |
| 지원내역 관리 | 등록 코드를 연결함. 실제 등록 요청·저장 확인, 상태·입력 검증·조회·수정·삭제는 남아 있음 |
| 회사·지원내역 관리 화면 | 사용 흐름 합의, 미구현 |
| 개발용 테이블 초기화 | `ddl-auto=create` 설정. 서버 시작 시 JPA가 관리하는 테이블을 삭제·재생성 |
| 자동 테스트 | 생성된 `contextLoads()` 테스트만 존재, 실행 결과 미확인 |

2026-09-17 노트북에서 사용하던 개발 환경을 새 컴퓨터에 다시 구성하여 DB 연결을 포함한 서버 실행을 확인하고, 첫 화면의 HTTP 200 응답과 `JobTracker` 제목을 다시 확인했습니다. 첫 화면 자체는 DB를 조회하지 않으므로, DB 연결은 별도로 시작 로그와 PostgreSQL의 JDBC 접속 세션을 확인했습니다.

## 사용 기술

| 구분 | 기술 | 현재 적용 범위 |
| --- | --- | --- |
| 언어 | Java 17 | 애플리케이션과 컨트롤러 작성 |
| 프레임워크 | Spring Boot 4.1.1, Spring Web MVC | 첫 화면과 회사 등록·목록 조회 REST API |
| 화면 | Mustache | 첫 화면 템플릿 |
| 데이터베이스 | PostgreSQL 18 | 개발용 DB 연결, `company` 테이블에 회사 정보 저장 |
| 데이터 접근 | Spring Data JPA | 회사 저장·조회, `JobApplication`의 회사 연관관계와 Repository 작성 |
| 입력 검증 | Validation | 요청 DTO의 회사명에 `@NotBlank`, 컨트롤러에 `@Valid` 적용 |
| 코드 작성 보조 | Lombok | 의존성 및 어노테이션 처리 설정 |
| 빌드 | Gradle Wrapper 9.7.1, Groovy DSL | 프로젝트 빌드 설정 |
| 버전 관리 | Git, GitHub | 소스 및 변경 이력 관리 |

## 구현 범위와 남은 기능

책 실습 코드와의 대응 관계, 각 계층의 역할, 요청 흐름과 다음 학습 순서는 [아키텍처와 학습 계획](ARCHITECTURE.md)에 정리했습니다. 회사 등록·목록 조회·회사명 검증까지 확인했으며, 아래 나머지 기능은 앞으로 개발할 범위입니다.

화면은 Mustache로 구성하고, 등록·수정·삭제 요청은 JavaScript의 `fetch()`로 REST API에 전달할 계획입니다. 화면용 컨트롤러와 REST API 컨트롤러는 같은 서비스를 사용하도록 구현합니다.

1. **회사 등록·목록 조회**: API와 회사명 필수 검증 구현. 회사 상세 조회·화면 연결과 중복 기준 결정은 남아 있습니다.
2. **지원내역 관리**: 회사를 선택해 직무·지원 날짜와 시각·메모를 기록하고, 목록·상세 조회와 상태 등을 포함한 수정·삭제를 구현합니다.
3. **입력 검증과 오류 처리**: 필수 값 누락이나 존재하지 않는 회사·지원 번호에 대한 요청을 처리하고 테스트합니다.
4. **검색·필터·페이징**: 기본 CRUD와 사용 가능한 화면을 완성한 뒤 검토할 확장 기능입니다.

### 데이터와 기능의 기준

- 회사 한 곳에 여러 지원내역을 연결합니다. 같은 회사의 다른 직무에 지원한 기록을 구분할 수 있도록 합니다.
- 지원 상태는 `지원완료`, `서류합격`, `면접`, `최종합격`, `불합격`, `지원취소`로 계획합니다.
- 첫 버전에서는 현재 상태를 저장하며, 상태 변경 이력은 포함하지 않습니다.
- 개인의 로컬 사용을 전제로 시작합니다. 로그인, 통계, 면접 일정, 배포는 기본 기능 완성 후 필요에 따라 검토합니다.
- 개발과 테스트에는 가상 회사·지원내역을 사용합니다.

## 현재 요청 흐름

```text
브라우저에서 GET /
    → HomeController.index()
    → 화면 이름 "home/home" 반환
    → templates/home/home.mustache로 HTML 생성
    → 브라우저에 JobTracker 제목 표시
```

첫 화면은 DB를 조회하지 않습니다. 회사 등록은 별도 API로 처리합니다.

```text
POST /api/companies + JSON
    → Spring이 요청 JSON을 CompanyRequestDto로 읽음
    → @Valid로 회사명 검사: 실패하면 기본 400 응답, 통과하면 아래 흐름 진행
    → CompanyApiController.createCompany(companyRequestDto)
    → CompanyService.create(companyRequestDto)
    → companyRequestDto.toEntity(): ID가 null인 새 Company 생성
    → companyRepository.save(target): JPA를 통해 PostgreSQL에 저장
    → 저장 결과가 Service에서 Controller의 createdCompany로 반환
    → ResponseEntity.status(HttpStatus.CREATED).body(createdCompany)
    → HTTP 201 + 저장된 회사 정보를 JSON으로 응답
```

`toEntity()`는 객체를 생성하는 메서드이며 직접 DB에 저장하지 않습니다. 현재는 별도 응답 DTO 없이 `Company`를 응답 본문에 사용합니다.

회사 목록 조회는 `CompanyApiController.indexCompanies()` → `CompanyService.index()` → `companyRepository.findAll()` 순서로 호출하고, 반환된 목록을 200 응답 본문에 담습니다. 회사가 없으면 빈 목록 `[]`을 반환합니다.

## API 명세와 직접 확인하기

| 기능 | 요청 | 상태 |
| --- | --- | --- |
| 첫 화면 | `GET /` | 구현 완료, HTML 응답 |
| 회사 등록 | `POST /api/companies` | 구현 완료, 성공 시 `201 Created` |
| 회사 목록 조회 | `GET /api/companies` | 구현, `200 OK`와 회사 목록 |
| 지원내역 등록 | `POST /api/companies/{companyId}/job-applications` | 등록 코드 작성, 실제 요청·저장 확인은 다음 작업 |

서버를 실행한 상태에서 Talend API Tester 등으로 다음 요청을 보냅니다.

```http
POST http://localhost:8080/api/companies
Content-Type: application/json

{
  "name": "직접등록한 테스트회사",
  "location": "성남"
}
```

성공 응답 예시입니다. ID는 DB가 생성하므로 실제 값은 달라질 수 있습니다.

```json
{
  "id": 1,
  "name": "직접등록한 테스트회사",
  "location": "성남"
}
```

회사명의 `""`·`"   "`·`null`은 현재 400으로 거절합니다. 본인이 Talend에서 각 요청의 응답을 확인했습니다. 오류 메시지 본문은 Spring의 기본 형식을 사용하며, 직접 만드는 예외 처리 메서드는 보류했습니다. 위치의 필수 여부와 중복 회사 판단 기준은 아직 정하지 않았으며, 동일한 정상 회사 정보를 다시 보내면 새 ID로 추가됩니다.

지원내역 등록은 아래 요청으로 확인할 예정입니다. `3`은 예시이며 먼저 등록한 회사의 실제 ID를 사용합니다. 회사 ID는 주소에서 받고, 본문에는 직무·지원 시각·메모만 보냅니다.

```http
POST http://localhost:8080/api/companies/3/job-applications
Content-Type: application/json

{
  "jobTitle": "백엔드 개발자",
  "appliedAt": "2026-09-23T14:30:00",
  "memo": "채용 홈페이지에서 지원"
}
```

지원내역 등록 흐름은 다음과 같습니다.

```text
주소의 회사 ID + 본문의 지원 정보
    → JobApplicationApiController가 두 값을 서비스에 전달
    → JobApplicationService가 기존 회사 조회
    → 회사가 없으면 예외를 발생시켜 저장 중단
    → 찾은 회사와 입력값으로 새 JobApplication 생성
    → JobApplicationRepository로 저장
    → 저장 결과를 201 응답 본문에 담음
```

현재 응답에는 저장한 엔티티 객체를 그대로 사용합니다. 나중에 응답용 DTO를 만들어 보낼 항목을 따로 정할 계획입니다. 지원내역의 직무·지원 시각 입력 검증은 아직 없고, 없는 회사의 예외를 404 등으로 바꾸는 처리도 아직 추가하지 않았습니다.

## 현재 소스 구조

```text
src/
├─ main/
│  ├─ java/com/example/jobtracker/
│  │  ├─ JobtrackerApplication.java
│  │  ├─ controller/
│  │  │  ├─ HomeController.java
│  │  │  ├─ CompanyApiController.java
│  │  │  └─ JobApplicationApiController.java
│  │  ├─ dto/
│  │  │  ├─ CompanyRequestDto.java
│  │  │  └─ JobApplicationRequestDto.java
│  │  ├─ entity/
│  │  │  ├─ Company.java
│  │  │  └─ JobApplication.java
│  │  ├─ repository/
│  │  │  ├─ CompanyRepository.java
│  │  │  └─ JobApplicationRepository.java
│  │  └─ service/
│  │     ├─ CompanyService.java
│  │     └─ JobApplicationService.java
│  └─ resources/
│     ├─ application.properties
│     └─ templates/home/home.mustache
└─ test/java/com/example/jobtracker/
   └─ JobtrackerApplicationTests.java
```

## 실행 설정

지원내역 등록 코드까지 연결했습니다. 지원내역을 실제로 저장하는 실행 확인은 다음 학습에서 진행하므로, 앞서 확인한 회사 API의 결과와 구분합니다.

현재 설정은 로컬 PostgreSQL의 `localhost:5432/jobtracker_db`에 `postgres` 계정으로 접속하도록 작성되어 있습니다. 실제 비밀번호는 소스에 적지 않고 환경변수 `DB_PASSWORD`로 전달합니다.

**현재 `spring.jpa.hibernate.ddl-auto=create`입니다. 실행할 때마다 JPA가 관리하는 테이블을 삭제하고 다시 생성하므로, 해당 테이블의 기존 데이터가 사라집니다. 지워도 되는 가상 데이터가 있는 로컬 학습용 DB에서만 사용합니다.** 실행 중에는 등록 요청마다 데이터가 쌓이며, 요청마다 초기화되는 것은 아닙니다. 재시작 후에도 테스트 데이터를 유지하고 싶어지면 로컬 설정을 `update`로 변경할 계획입니다. 이미 삭제된 데이터는 `update`로 바꿔도 복구되지 않습니다.

1. `build.gradle`이 있는 폴더를 IntelliJ에서 Gradle 프로젝트로 연결합니다.
2. JDK 17을 설치하고 프로젝트 SDK 및 Gradle 실행 JDK를 17로 맞춥니다.
3. 로컬 PostgreSQL을 실행하고 `jobtracker_db`를 준비합니다. DB가 없다면 `postgres` 등 DB 생성 권한이 있는 계정으로 `CREATE DATABASE jobtracker_db;`를 실행합니다.
4. `JobtrackerApplication` 실행 설정의 환경 변수에 `DB_PASSWORD`를 추가하고, 로컬 `postgres` 계정의 비밀번호를 직접 입력합니다. 비밀번호가 들어간 실행 설정은 프로젝트 공유 파일로 저장하지 않습니다.
5. `JobtrackerApplication`을 실행합니다. 시작 로그의 `HikariPool-1 - Start completed.`와 DB URL, `Started JobtrackerApplication`을 확인합니다.
6. `http://localhost:8080/`에 접속하여 첫 화면을 확인합니다.

2026-09-17에는 JDK 17의 Gradle `bootRun`으로 서버 실행과 DB 연결을 검증했습니다. 2026-09-21에는 회사 등록 코드의 컴파일·실행·HTTP 응답·DB 저장을 확인했고, 본인도 서버를 실행하고 Talend API Tester에서 직접 요청을 보냈습니다. 이 등록 검증은 `ddl-auto=update`일 때 진행했으며, 이후 반복 학습을 위해 설정을 `create`로 변경했습니다. 변경 후 테이블 재생성 동작을 별도로 재검증한 것은 아닙니다.

## 진행 기록과 다음 시작점

### 2026-09-17: 새 컴퓨터에서 실행 환경과 DB 연결 재설정

노트북 고장으로 컴퓨터를 교체하면서, 노트북에서 이미 진행했던 PostgreSQL 연결을 새 컴퓨터에서 다시 설정하고 검증한 작업입니다. 이날의 작업 범위는 기존 개발 환경 재구성과 연결 재확인이며, 새로운 CRUD 기능 구현은 포함하지 않습니다.

- 직접 진행한 설정: IntelliJ에서 Temurin JDK 17 설치·선택, Gradle 프로젝트 연결, 실행 설정에 `DB_PASSWORD` 입력.
- AI 도구로 진행한 작업: 접속 시 `jobtracker_db`가 없음을 확인하여 빈 DB 생성, `psql` 쿼리로 DB명·접속 계정 확인, Gradle `bootRun`으로 Spring Boot 실행 검증.
- 확인 결과: `jobtracker_db` / `postgres`로 SQL 요청 성공, HikariCP의 PostgreSQL 연결 생성과 JPA 초기화 성공, PostgreSQL에서 JDBC 접속 세션 확인, 첫 화면 HTTP 200 응답 확인.
- 회사·지원내역의 Entity, Repository, Service, CRUD API와 업무용 테이블은 아직 구현하지 않았습니다. 자동 테스트는 이번 확인에서 실행하지 않았습니다.
- 다음 학습 시작점: 회사명과 위치를 저장할 `Company` Entity의 역할을 이해하고 직접 작성한 뒤, 회사 등록·목록 조회 기능을 단계별로 구현합니다.

### 2026-09-21: 회사 등록 API 구현과 직접 요청 테스트

- AI의 설명과 코드 검토를 받으며 직접 작성: `Company`, `CompanyRepository`, `CompanyRequestDto`, `CompanyService`, `CompanyApiController`.
- 학습한 흐름: 요청 JSON → DTO → 서비스 → Entity 변환 → Repository 저장 → 컨트롤러의 HTTP 응답.
- `JpaRepository<Company, Long>`의 저장 기능과 `toEntity()`의 역할을 구분하고, 서비스의 `save()` 반환값이 컨트롤러로 전달되는 과정을 확인했습니다.
- `ResponseEntity.ok(...)`와 `status(HttpStatus.OK).body(...)`는 200 응답이며, 새 회사 등록에는 `status(HttpStatus.CREATED).body(...)`로 201을 반환하도록 작성했습니다.
- AI 도구로 확인: Java 17 컴파일·서버 실행, 회사 등록 요청의 201 응답, 자동 생성 ID, 응답 값과 PostgreSQL 저장 값의 일치.
- 본인이 직접 확인: Talend API Tester로 정상 값·빈 문자열·`null` 요청을 보내고, 같은 내용도 새 ID로 추가되는 동작을 관찰했습니다.
- 다음에 필요한 규칙: 회사명 필수 검증, 위치 필수 여부 결정, 중복 회사 판단 기준과 처리 방식 결정.
- `ddl-auto=update`와 `create`의 차이를 학습하고, 반복 학습 중 서버 시작마다 초기화하도록 로컬 설정을 `create`로 변경했습니다.
- 자동 테스트는 이번에 실행하지 않았습니다. 현재 기록한 결과는 실제 서버에 대한 수동·도구 기반 검증입니다.

### 2026-09-23 기준: 검증·조회 확인과 지원내역 준비

- 회사명 검증을 추가하고, 본인이 정상 요청의 회사 정보 응답과 빈 문자열·공백·`null`의 400 응답을 확인했습니다.
- 회사 목록 조회를 구현하고 본인이 Talend로 목록이 나오는 것을 확인했습니다.
- 책의 게시글·댓글 관계와 비교해 회사·지원내역 연관관계를 학습했습니다.
- `JobApplication`에 `id`, `company`, `jobTitle`, `appliedAt`, `memo`를 작성했습니다. 날짜뿐 아니라 시각도 기록하려고 `LocalDateTime`을 선택했습니다.
- `JobApplicationRepository`와 `JobApplicationRequestDto`를 작성했습니다. 처음에는 DTO에 `companyId`를 넣었지만, 회사별 등록 주소를 사용하기로 하면서 회사 ID는 주소로 받고 DTO에서는 제거했습니다.
- 회사 목록에서 상세 화면으로 들어가 지원내역을 기록·관리하는 사용자 흐름을 합의했습니다. 상태의 초기값은 `지원완료`로 계획했으며 필드·처리는 아직 미구현입니다.
- 위 검증·조회 결과는 이전 단계에서 확인한 기록입니다. 이번 문서 갱신에서는 서버를 실행하거나 DB를 변경하지 않았습니다.

### 2026-09-23: 지원내역 등록 코드를 연결하며 배운 것

- 지원내역 등록 주소를 `POST /api/companies/{companyId}/job-applications`로 정했습니다. 회사 ID는 주소에서 받고, 직무·지원 날짜와 시각·메모는 요청 본문에서 받습니다.
- 상세보기를 새 페이지로 열지 현재 화면에서 펼칠지와, API 주소를 정하는 일은 별개라는 점을 배웠습니다. 기존 기록을 보는 요청은 GET, 새 기록을 저장하는 요청은 POST로 구분합니다.
- 회사들 중 하나를 고르는 의미로 주소에 `companies`를 쓰기로 했습니다. 복수형은 관례이며, 단수형이면 실행되지 않는 필수 문법은 아닙니다.
- 서비스에서는 먼저 할 일을 주석으로 적고 코드로 옮겼습니다: 회사 찾기 → 없으면 중단 → 지원내역 객체 만들기 → 저장하고 반환하기.
- `findById(companyId)`로 회사를 찾고, 없으면 `orElseThrow(...)`로 예외를 발생시켰습니다. 이 예외의 HTTP 응답을 따로 정하는 작업은 남아 있습니다.
- `createJobApplication(...)`은 찾은 회사 객체와 입력값을 받아 새 지원내역 객체를 만듭니다. 이 메서드 자체는 DB 저장을 하지 않고, 서비스의 `save(...)`가 저장을 담당합니다.
- 새 지원내역의 ID는 DB에서 생성하므로 생성자에는 `null`을 넣습니다. 연결할 회사는 이미 조회한 기존 객체를 사용합니다.
- 요청 DTO에 지원내역 ID를 넣지 않고, 회사 ID도 주소 한 곳에서만 받으므로 책의 댓글 코드에 있던 두 ID 검사를 그대로 복사할 필요가 없다는 점을 확인했습니다.
- 커밋 전 AI가 남아 있던 댓글 검사 코드(`getId()`와 `article` 참조)를 제거했습니다. 현재 DTO와 엔티티에는 맞지 않아 컴파일을 막는 코드였으며 새 기능을 추가한 수정은 아닙니다.
- 지금은 저장 결과를 엔티티 객체로 응답하며, 응답용 DTO는 기본 흐름을 확인한 뒤 이유를 이해하면서 추가합니다.
- 회사명 검증·목록 조회의 실행 결과는 이전 단계에서 확인한 기록입니다. 이번 마무리에서는 Java 17로 `gradlew.bat compileJava`를 실행해 성공을 확인했습니다. 서버를 실행하거나 DB를 변경하지 않았으며, 지원내역 등록의 실제 201 응답·DB 저장과 자동 테스트는 아직 확인하지 않았습니다.

### 다음 시작점과 학습 방식

다음에는 서버를 실행하고 먼저 가상 회사 한 곳을 등록합니다. 반환된 회사 ID로 `POST /api/companies/{companyId}/job-applications` 요청을 보내, 201 응답과 지원내역 ID·직무·지원 시각·메모·연결된 회사·실제 DB 저장을 확인합니다. `ddl-auto=create`이므로 서버 재시작 후 회사부터 다시 등록해야 합니다. 정상 등록을 확인한 다음 없는 회사 처리, 지원내역 입력 검증, 상태 필드·초기값, 조회·수정·삭제와 응답용 DTO를 한 단계씩 이어갑니다.

- 목표는 설명을 듣고 본인이 직접 CRUD 코드를 작성하며, 요청부터 DB 저장과 응답까지의 흐름을 설명할 수 있게 되는 것입니다.
- 매 단계는 역할과 필요성 설명 → 직접 코드 작성 → 질문과 코드 검토 → 실행 확인 순서로 진행합니다. AI가 여러 계층의 완성 코드를 한꺼번에 구현하지 않습니다.
- 요청 내용을 먼저 정하고 Controller에서 필요한 Service 메서드를 생각하는 순서로 학습합니다. 클래스 작성 순서를 고정하지 않고, 본인이 요청 흐름을 따라 이해하기 쉬운 순서로 진행합니다.
- 책 실습을 한 번 마친 입문 수준을 기준으로 설명합니다. 새 개념을 여러 개 한꺼번에 쓰지 않고, 새 타입·메서드는 역할을 먼저 설명한 뒤 하나씩 적용합니다.
- 회사 등록과 목록 조회를 완성한 뒤 지원내역 등록·조회·수정·삭제로 확장합니다.
- 각 학습이 끝나면 직접 작성한 내용, 이해한 흐름, 남은 질문과 다음 시작점을 기록하고 커밋합니다.
- 커밋 메시지는 한국어로 작성하고, 새 기능 구현과 기존 환경 재설정을 구분하여 실제 작업 내용을 담습니다.

## 학습 및 작성 방식

《코딩 자율학습 스프링 부트 3 자바 백엔드 개발 입문》의 게시글·댓글 실습을 참고하여 회사·지원내역 관리로 응용하는 개인 학습 프로젝트입니다.

회사 등록 코드는 AI의 단계별 설명·예시·코드 검토를 받으며 직접 작성했습니다. 이번 구현을 백지에서 혼자 설계한 경험으로 표현하지 않으며, 다음에는 요청부터 저장·응답까지 설명하고 핵심 메서드를 다시 작성해 보는 것을 목표로 합니다. 프로젝트 기본 파일은 Spring Initializr로 생성했습니다.
