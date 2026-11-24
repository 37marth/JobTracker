# JobTracker

사람인·잡코리아 등에 흩어진 취업 지원 내역을 **회사별로 한곳에 모아 관리**하는 Spring Boot 웹 서비스입니다.
회사를 등록하고, 회사마다 지원한 직무·날짜·메모·진행 상태를 기록합니다.

> 개인 프로젝트 · 진행 중 (회원 기능 개발 중, 첫 배포 전)

## 기술 스택

Java 17 · Spring Boot 4.1.1 (Web MVC, Data JPA, Validation) · PostgreSQL · Mustache · Spring Security(BCrypt, 로그인 설정 진행 중) · Lombok · Gradle

## 주요 기능

- **회사 관리**: 등록·조회·수정·삭제. 같은 이름의 회사는 409로 거절하고, 수정할 때는 자기 자신을 제외하고 검사합니다.
- **지원내역 관리**: 회사별 직무·지원일·메모 등록·수정·삭제. 회사를 삭제하면 연결된 지원내역도 함께 삭제합니다.
- **지원 상태**: 지원완료·서류합격·면접·최종합격·불합격·지원취소 중 선택하면 바로 저장됩니다.
- **입력 검증과 오류 안내**: 잘못된 입력은 400, 수정·삭제할 대상이 없으면 404로 응답합니다. 입력 오류는 화면에 이유를 보여주고 입력값을 유지합니다.
- **페이징**: 최신 등록 회사부터 5개씩, 페이지 번호 5개 묶음과 처음·이전·다음·마지막 이동. 잘못된 페이지 번호와 삭제 후 빈 페이지를 보정합니다.
- **회원가입 API** (진행 중): 아이디 중복 검사(409), 비밀번호 BCrypt 해시 저장.

## 구조

```
브라우저(Mustache 화면) → Controller → Service → Repository → PostgreSQL
```

- `Company` 1 : N `JobApplication`
- 요청·응답은 DTO로 분리하고, 예외는 `GlobalExceptionHandler`에서 상태 코드와 안내 문장으로 변환합니다.
- 자세한 설계: [ARCHITECTURE.md](ARCHITECTURE.md)

## 개발하며 해결한 문제

자세한 과정(문제 → 원인 → 선택 → 확인)은 [문제 해결 기록](docs/troubleshooting.md)에 있습니다.

| 문제 | 해결 |
| --- | --- |
| 회사를 수정하면 목록 순서가 바뀜 | 수정해도 변하지 않는 ID를 정렬 기준으로 사용 |
| 같은 회사가 여러 번 등록됨 | 이름 기준 중복 검사, 수정 시 자기 ID 제외 |
| 입력이 실패해도 이유를 알 수 없음 | 오류 이유를 응답에 담아 입력 영역에 표시 |
| 페이지 번호가 범위를 벗어나거나 삭제 후 빈 페이지가 남음 | 음수는 첫 페이지, 범위 초과·삭제 후에는 마지막 페이지로 보정 |

## API

| 메서드 | 경로 | 기능 | 성공 |
| --- | --- | --- | --- |
| GET | `/` | 회사·지원내역 화면 (`?page=0`부터) | 200 |
| POST | `/api/companies` | 회사 등록 | 201 |
| GET | `/api/companies` | 회사 목록 | 200 |
| PATCH | `/api/companies/{id}` | 회사 수정 | 200 |
| DELETE | `/api/companies/{id}` | 회사 삭제(지원내역 포함) | 204 |
| POST | `/api/companies/{companyId}/job-applications` | 지원내역 등록 | 201 |
| GET | `/api/companies/{companyId}/job-applications` | 회사별 지원내역 목록 | 200 |
| PATCH | `/api/job-applications/{id}` | 지원내역 수정 | 200 |
| PATCH | `/api/job-applications/{id}/status` | 지원 상태 변경 | 200 |
| DELETE | `/api/job-applications/{id}` | 지원내역 삭제 | 204 |
| POST | `/api/members/signup` | 회원가입 | 201 |

잘못된 입력(400)과 중복(409)은 `{"message": "안내 문장"}`으로 이유를 응답합니다. 수정·삭제할 대상이나 지원내역을 등록할 회사가 없으면 404를 응답합니다(안내 문장 없음). 회사별 지원내역 목록은 없는 회사 ID여도 200과 빈 목록을 반환합니다. IntelliJ HTTP Client용 요청 예시와 테스트 순서는 [`http/`](http) 폴더에 있습니다.

## 실행 방법

필요: JDK 17, PostgreSQL

1. PostgreSQL에 `jobtracker_db`를 만듭니다.
2. 실행 환경변수에 `DB_PASSWORD`(postgres 계정 비밀번호)를 설정합니다. 접속 주소·계정은 `src/main/resources/application.properties`에서 바꿀 수 있습니다.
3. `JobtrackerApplication`을 실행하고 `http://localhost:8080/`에 접속합니다.

> 현재 보안 설정은 회원가입 POST의 비로그인 접근을 허용하고, 나머지 요청에는 로그인을 요구합니다. 로그인 화면 설정과 CSRF 토큰 연동이 진행 중이므로 홈 화면 접근과 회원가입 POST 실행에는 추가 설정이 필요합니다.

> 현재는 개발 설정(`ddl-auto=create`)이라 **서버를 시작할 때마다 테이블이 초기화**되고 예시 회사 26개가 들어갑니다. 데이터를 유지하려면 로컬에서 `ddl-auto=update`, `spring.sql.init.mode=never`로 바꿉니다.

## 진행 상황과 다음 계획

- [x] 회사·지원내역 CRUD, 입력 검증, 오류 응답, 중복 검사
- [x] 회사 목록 페이징과 경계 처리
- [x] 회원가입 API (중복 검사, BCrypt 해시 저장)
- [ ] 세션 기반 로그인·로그아웃 (Spring Security)
- [ ] 회원별 데이터 분리와 접근 제한
- [ ] 데이터 보존 설정과 첫 배포
- [ ] 구글 소셜 로그인 (첫 배포 이후)

## 알려진 한계

- 회원별 데이터 분리는 구현 중이며, 회사·지원내역은 아직 공통 데이터로 관리됩니다.
- 회사명 중복은 서비스에서 검사하며, DB 고유 제약은 아직 없습니다(동시 요청 중복은 미검증).
- 자동 테스트는 오류 응답과 페이징 위주입니다. 회원 기능·실제 DB 제약 테스트는 추가 예정입니다.
