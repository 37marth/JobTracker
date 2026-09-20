# JobTracker

**회사별 취업 지원내역과 진행 상태를 한곳에서 관리하기 위해 개발 중인 Spring Boot 웹 프로젝트입니다.**

지원한 회사, 직무, 지원일, 현재 결과와 메모를 기록하고 회사명과 상태로 필요한 지원내역을 찾아보는 것을 목표로 합니다.

> 현재는 회사 등록 API를 작성하고, HTTP 201 응답과 PostgreSQL 저장을 확인한 단계입니다. 입력 검증, 중복 처리, 회사 목록 조회와 지원내역 CRUD는 아직 구현하지 않았습니다.

최종 갱신: 2026-09-21

## 프로젝트 목표

다음과 같은 질문을 기록을 통해 확인할 수 있도록 만드는 것이 서비스의 목표입니다.

- 어느 회사의 어떤 직무에 언제 지원했는가?
- 각 지원의 현재 상태는 무엇인가?
- 같은 회사에 지원한 다른 내역이 있는가?
- 지원할 때 남긴 메모는 무엇인가?

개발 과정에서는 Spring MVC와 REST API의 요청 처리, JPA를 이용한 데이터 저장, 입력 검증과 오류 처리, 테스트를 통한 동작 확인을 익히고 적용합니다.

## 현재 구현 상태

| 항목 | 상태 |
| --- | --- |
| Spring Boot 프로젝트 생성 및 서버 실행 | 초기 서버 실행 확인 |
| 첫 화면 | `GET /` 요청에 `JobTracker` 제목 표시, HTTP 200 응답 확인 |
| PostgreSQL 준비 | 개발용 `jobtracker_db` 생성, 드라이버와 접속 설정 추가 |
| 애플리케이션의 DB 연결 | 노트북에서 진행했던 연결을 2026-09-17 새 컴퓨터에서 재설정하고 PostgreSQL 연결 및 JPA 초기화 재확인 |
| 회사 등록 | `POST /api/companies` 구현, HTTP 201·생성된 ID·DB 저장 확인 |
| 입력 검증·중복 처리 | 미구현. 빈 문자열·`null`도 등록되고 같은 내용을 다시 보내면 새 ID로 등록됨 |
| 회사 목록 조회·지원내역 관리 | 미구현 |
| 개발용 테이블 초기화 | `ddl-auto=create` 설정. 서버 시작 시 JPA가 관리하는 테이블을 삭제·재생성 |
| 자동 테스트 | 생성된 `contextLoads()` 테스트만 존재, 실행 결과 미확인 |

2026-09-17 노트북에서 사용하던 개발 환경을 새 컴퓨터에 다시 구성하여 DB 연결을 포함한 서버 실행을 확인하고, 첫 화면의 HTTP 200 응답과 `JobTracker` 제목을 다시 확인했습니다. 첫 화면 자체는 DB를 조회하지 않으므로, DB 연결은 별도로 시작 로그와 PostgreSQL의 JDBC 접속 세션을 확인했습니다.

## 사용 기술

| 구분 | 기술 | 현재 적용 범위 |
| --- | --- | --- |
| 언어 | Java 17 | 애플리케이션과 컨트롤러 작성 |
| 프레임워크 | Spring Boot 4.1.1, Spring Web MVC | 첫 화면과 회사 등록 REST API |
| 화면 | Mustache | 첫 화면 템플릿 |
| 데이터베이스 | PostgreSQL 18 | 개발용 DB 연결, `company` 테이블에 회사 정보 저장 |
| 데이터 접근 | Spring Data JPA | `Company`, `JpaRepository<Company, Long>` 기반 저장 |
| 입력 검증 | Validation | 의존성 추가, 입력 검증 구현 예정 |
| 코드 작성 보조 | Lombok | 의존성 및 어노테이션 처리 설정 |
| 빌드 | Gradle Wrapper 9.7.1, Groovy DSL | 프로젝트 빌드 설정 |
| 버전 관리 | Git, GitHub | 소스 및 변경 이력 관리 |

## 구현 범위와 남은 기능

책 실습 코드와의 대응 관계, 각 계층의 역할, 요청 흐름과 다음 학습 순서는 [아키텍처와 학습 계획](ARCHITECTURE.md)에 정리했습니다. 기본 회사 등록은 동작하며, 아래 나머지 기능은 앞으로 개발할 범위입니다.

화면은 Mustache로 구성하고, 등록·수정·삭제 요청은 JavaScript의 `fetch()`로 REST API에 전달할 계획입니다. 화면용 컨트롤러와 REST API 컨트롤러는 같은 서비스를 사용하도록 구현합니다.

1. **회사 등록·목록 조회**: 기본 등록 API 구현 완료. 입력 검증·중복 기준 결정·목록 조회·회사 화면 연결은 남아 있습니다.
2. **지원내역 관리**: 회사를 선택해 직무·지원일·상태·메모를 등록하고, 목록·상세 조회와 수정·삭제를 구현합니다.
3. **입력 검증과 오류 처리**: 필수 값 누락이나 존재하지 않는 회사·지원 번호에 대한 요청을 처리하고 테스트합니다.
4. **검색·필터·페이징**: 회사명과 지원 상태로 조회하고, 정해진 순서로 목록을 나누어 표시합니다.

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
    → CompanyApiController.createCompany(companyRequestDto)
    → CompanyService.create(companyRequestDto)
    → companyRequestDto.toEntity(): ID가 null인 새 Company 생성
    → companyRepository.save(target): JPA를 통해 PostgreSQL에 저장
    → 저장 결과가 Service에서 Controller의 createdCompany로 반환
    → ResponseEntity.status(HttpStatus.CREATED).body(createdCompany)
    → HTTP 201 + 저장된 회사 정보를 JSON으로 응답
```

`toEntity()`는 객체를 생성하는 메서드이며 직접 DB에 저장하지 않습니다. 현재는 별도 응답 DTO 없이 `Company`를 응답 본문에 사용합니다.

## API 명세와 직접 확인하기

| 기능 | 요청 | 상태 |
| --- | --- | --- |
| 첫 화면 | `GET /` | 구현 완료, HTML 응답 |
| 회사 등록 | `POST /api/companies` | 구현 완료, 성공 시 `201 Created` |
| 회사 목록 조회 | `GET /api/companies` | 계획, 아직 호출할 수 없음 |

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

현재 같은 요청을 반복하면 새 ID로 회사가 추가됩니다. 회사명과 위치의 `""`·`null`도 허용되는 것을 직접 확인했습니다. 입력 검증과 중복 방지는 다음 구현 대상이며, 아직 `400`·`409` 응답을 구현한 것은 아닙니다. 회사명 필수 검증부터 추가하고, 위치 필수 여부와 중복 회사의 기준은 별도로 결정합니다.

## 현재 소스 구조

```text
src/
├─ main/
│  ├─ java/com/example/jobtracker/
│  │  ├─ JobtrackerApplication.java
│  │  ├─ controller/
│  │  │  ├─ HomeController.java
│  │  │  └─ CompanyApiController.java
│  │  ├─ dto/CompanyRequestDto.java
│  │  ├─ entity/Company.java
│  │  ├─ repository/CompanyRepository.java
│  │  └─ service/CompanyService.java
│  └─ resources/
│     ├─ application.properties
│     └─ templates/home/home.mustache
└─ test/java/com/example/jobtracker/
   └─ JobtrackerApplicationTests.java
```

## 실행 설정

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

### 다음 시작점과 학습 방식

다음에는 `CompanyRequestDto.name`에 `@NotBlank`, 컨트롤러의 요청 DTO 매개변수에 `@Valid`를 적용하는 이유부터 배우고 직접 작성합니다. `null`, `""`, `"   "`을 보내 400 응답과 신규 저장 방지를 확인하고, 정상 요청은 계속 201로 등록되는지 확인합니다. 아직 이 검증 코드는 추가하지 않았습니다.

- 목표는 설명을 듣고 본인이 직접 CRUD 코드를 작성하며, 요청부터 DB 저장과 응답까지의 흐름을 설명할 수 있게 되는 것입니다.
- 매 단계는 역할과 필요성 설명 → 직접 코드 작성 → 질문과 코드 검토 → 실행 확인 순서로 진행합니다. AI가 여러 계층의 완성 코드를 한꺼번에 구현하지 않습니다.
- 요청 내용을 먼저 정하고 Controller에서 필요한 Service 메서드를 생각하는 순서로 학습합니다. 클래스 작성 순서를 고정하지 않고, 본인이 요청 흐름을 따라 이해하기 쉬운 순서로 진행합니다.
- 회사 등록과 목록 조회를 완성한 뒤 지원내역 등록·조회·수정·삭제로 확장합니다.
- 각 학습이 끝나면 직접 작성한 내용, 이해한 흐름, 남은 질문과 다음 시작점을 기록하고 커밋합니다.
- 커밋 메시지는 한국어로 작성하고, 새 기능 구현과 기존 환경 재설정을 구분하여 실제 작업 내용을 담습니다.

## 학습 및 작성 방식

《코딩 자율학습 스프링 부트 3 자바 백엔드 개발 입문》의 게시글·댓글 실습을 참고하여 회사·지원내역 관리로 응용하는 개인 학습 프로젝트입니다.

회사 등록 코드는 AI의 단계별 설명·예시·코드 검토를 받으며 직접 작성했습니다. 이번 구현을 백지에서 혼자 설계한 경험으로 표현하지 않으며, 다음에는 요청부터 저장·응답까지 설명하고 핵심 메서드를 다시 작성해 보는 것을 목표로 합니다. 프로젝트 기본 파일은 Spring Initializr로 생성했습니다.
