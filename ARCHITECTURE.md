# JobTracker: 책 실습을 응용하는 아키텍처와 학습 계획

작성일: 2026-09-18

최종 갱신: 2026-09-21

## 1. 목표와 현재 출발점

목표는 Java / Spring Boot 신입 백엔드 취업에 활용할 수 있는 작은 프로젝트를 본인이 직접 만들고, 작성한 CRUD 코드의 역할과 실행 흐름을 설명할 수 있게 되는 것입니다.

출발점은 `SpringPractice-main`에서 책을 보며 작성한 게시글·댓글 실습입니다. 실습 코드가 존재한다는 사실만으로 모든 개념을 이해하거나 백지에서 구현할 수 있다고 가정하지 않습니다.

학습은 기존 코드와 비교 → 필요한 개념 설명 → 본인이 직접 작성 → AI가 저장된 코드 검토 → 실행 결과 확인 순서로 진행합니다. 모르는 용어가 나오면 그 자리에서 설명하고, 새 설정은 왜 필요한지 먼저 설명합니다.

현재 잡트래커에는 첫 화면과 회사 등록 API가 있습니다. `Company`, `CompanyRepository`, `CompanyRequestDto`, `CompanyService`, `CompanyApiController`를 AI의 설명과 검토를 받으며 직접 작성했습니다. PostgreSQL의 `company` 테이블 생성과 실제 저장, 회사 등록의 HTTP 201 응답을 확인했습니다. 회사 목록 조회·입력 검증·중복 처리·지원내역 CRUD는 아직 미구현입니다.

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
| 6 | 회사명 입력 검증 | 다음 작업: `@NotBlank`·`@Valid`를 배우고 직접 적용 |
| 7 | 중복 기준과 처리 결정 | 회사명만 볼지 위치도 볼지 등 규칙부터 논의, 미구현 |
| 8 | 회사 목록 조회 | 미구현, 조회 결과가 응답으로 돌아오는 과정 학습 |
| 9 | 회사 화면 연결 | 미구현, Mustache와 fetch의 역할부터 학습 |

현재 회사 등록 API 명세는 다음과 같습니다. 목록 조회 `GET /api/companies`는 계획만 있고 아직 구현하지 않았습니다. URL의 `companies`와 DB 테이블 이름 `company`는 서로 같을 필요가 없습니다.

| 항목 | 회사 등록 |
| --- | --- |
| 요청 | `POST /api/companies` |
| Content-Type | `application/json` |
| 요청 본문 예시 | `{"name":"샘플회사","location":"판교"}` |
| 요청 DTO | `CompanyRequestDto` |
| 처리 | DTO를 새 `Company`로 변환하고 Repository로 저장 |
| 성공 응답 | `201 Created`, 본문에 생성된 `id`, `name`, `location` |
| 현재 한계 | 빈 문자열·`null` 입력과 중복 내용 등록을 차단하지 않음 |

본인이 `JpaRepository`가 CRUD 메서드를 제공한다는 책 내용을 떠올려 선택했으므로 현재 구현을 유지합니다. 목록 조회와 페이징 기능은 사용할 때 필요한 메서드를 설명합니다.

요청 DTO는 책의 `ArticleForm`과 비교해 배우되 실제 이름은 `CompanyRequestDto`입니다. 기본 생성자·전체 필드 생성자·getter를 Lombok으로 선언했으며, 실제 JSON 요청을 DTO로 읽어 저장하는 흐름을 확인했습니다.

DTO 변환 위치, Service의 객체 주입 방식, 반환 타입은 실제 메서드 하나를 작성할 때 설명합니다. 처음부터 여러 요청·응답 DTO, 예외 처리 클래스, 생성자 변경을 동시에 요구하지 않습니다. 응답 DTO 분리는 기본 요청·응답 흐름을 확인한 뒤 이유와 함께 추가합니다.

## 5. 그다음 목표: 지원내역 CRUD

회사 등록과 조회를 이해한 다음 지원내역으로 확장합니다. 회사 한 곳에 지원내역 여러 건을 연결합니다. 이는 책의 게시글과 댓글 관계를 비교 대상으로 삼아 설명합니다.

지원내역에 필요한 정보는 고유 번호, 회사, 직무, 지원일, 현재 상태, 메모입니다. 필드 타입과 관계 어노테이션은 지원내역을 만드는 시점에 하나씩 정합니다.

| 기능 | 목표 |
| --- | --- |
| 등록(Create) | 회사를 선택해 지원내역 한 건 저장 |
| 조회(Read) | 지원내역 목록과 상세 확인 |
| 수정(Update) | 직무·상태·메모 등의 변경을 DB에 반영 |
| 삭제(Delete) | 선택한 지원내역 삭제, 연결된 회사는 유지 |

지원 상태는 기존 계획대로 지원완료, 서류합격, 면접, 최종합격, 불합격, 지원취소입니다. enum을 사용할 때는 먼저 문자열과 어떤 차이가 있고 왜 필요한지 설명합니다.

## 6. 기본 흐름 이후에 추가할 것

기본 저장·조회가 된다는 것은 중간 학습 목표입니다. 포트폴리오의 완료 기준에는 아래 보완도 포함하지만, 첫 Entity를 작성할 때 모두 요구하지 않습니다.

당장 다음 작업은 회사명 필수 검증입니다. DTO의 `name`에 `@NotBlank`, 컨트롤러의 DTO 매개변수에 `@Valid`를 적용하는 역할부터 배웁니다. 정상 요청의 201을 유지하면서 `null`·빈 문자열·공백 문자열을 400으로 거절하고 저장하지 않는지 확인합니다. 위치 필수 여부와 중복 기준은 별도 결정 사항입니다.

`findById(...).orElseThrow(...)`는 조회한 데이터가 없을 때 처리하는 방식이며, 등록 요청의 빈 값 검증과 구분합니다. 없는 ID 처리와 응답 메시지는 조회·수정·삭제 기능을 구현할 때 배웁니다.

- 회사명·직무 등 필수 입력, 문자열 길이, 공백 입력 검증.
- 존재하지 않는 회사·지원내역 요청에 대한 응답.
- 요청 DTO와 응답 DTO를 나누는 이유와 구현.
- DB 작업의 트랜잭션: 여러 작업을 함께 확정하거나 되돌려야 하는 이유.
- 정상 저장, 잘못된 입력, 없는 ID, 수정·삭제 결과를 확인하는 테스트.
- 회사명 검색, 지원 상태 필터, 페이징.

초기의 상세 설계에 있던 별도 화면/API 예외 처리기, 고정된 컬럼 길이, 모든 DTO 클래스명, 세부 HTTP 응답 형식은 이 단계에서 다시 검토합니다. 학습 전에 외워야 할 규칙으로 취급하지 않습니다.

개인의 로컬 사용으로 시작합니다. 로그인, 회사 수정·삭제, 상태 변경 이력, 통계, 면접 일정, 배포는 기본 기능 완료 후 검토합니다.

## 7. 코드 검토와 기록 원칙

- AI는 본인이 작성하고 저장한 프로젝트 파일과 설정을 직접 확인합니다. 확인 가능한 코드를 매번 채팅에 복사하도록 요구하지 않습니다.
- 검토에서는 맞게 응용한 부분, 실제 오류, 나중에 개선할 부분을 구분합니다. 새로운 설계 취향을 필수 수정사항처럼 제시하지 않습니다.
- 새 문법·어노테이션·기술은 목적과 필요성을 설명한 뒤 도입합니다.
- 기능 코드는 본인이 직접 작성하고, AI는 요청에 맞춰 설명·힌트·최소 수정·검토를 제공합니다.
- AI가 수행한 환경 설정이나 검증을 본인이 독립적으로 구현한 경험으로 기록하지 않습니다.
- 각 학습 후 직접 작성한 부분, 이해한 내용, 남은 질문, 다음 시작점을 남깁니다.
- 커밋 메시지는 한국어로 쓰고 환경 재설정과 새 기능 구현을 구분합니다.
