# 문제 해결 기록

## 회사 정보를 수정하면 목록 위치가 바뀌는 문제

- 기록일: 2026-10-12
- 진행 상태: 코드 적용 완료. 회사 수정 후 목록 순서 유지와 ID 오름차순 표시를 사용자 수동 확인.

### 겪은 문제

회사 목록이 `회사 1 → 회사 2 → 회사 3` 순서로 보였다. 회사 1의 정보를 수정하고 화면을 다시 불러오니 `회사 2 → 회사 3 → 회사 1`로 바뀌었다.

사용자는 이름이나 위치만 고쳤는데 목록 위치까지 바뀌면 수정한 회사를 다시 찾아야 한다. 회사 정보를 수정해도 목록 위치가 유지되어야 한다고 판단했다.

### 원인

`CompanyService.index()`는 `companyRepository.findAll()`로 회사 목록을 조회하고 있었다. 조회할 때 정렬 기준을 지정하지 않아 DB에서 반환하는 순서가 보장되지 않았다. 화면은 받은 목록 순서대로 회사를 표시하고 있었다.

### 선택한 해결 방법

회사 ID가 작은 순서대로 조회하도록 정렬 기준을 추가하기로 했다. 회사 이름과 위치를 수정해도 ID는 바뀌지 않으므로, 같은 회사 목록의 순서를 유지할 수 있다.

이름순은 이름을 수정하면 위치가 바뀔 수 있고, 수정 시각순도 수정할 때마다 위치가 바뀔 수 있다. 현재 요구사항에는 ID순이 맞다고 판단했다. 사용자가 직접 순서를 바꾸는 기능은 이번 작업 범위에 포함하지 않는다.

`CompanyRepository`에 다음 메서드를 추가하고 `CompanyService.index()`에서 호출하도록 변경했다.

```java
// CompanyRepository: java.util.List import 필요
List<Company> findAllByOrderByIdAsc();

// CompanyService.index(): 기존 findAll() 호출을 교체
List<Company> companies = companyRepository.findAllByOrderByIdAsc();
```

`OrderByIdAsc`는 ID 기준 오름차순 정렬을 뜻하며, SQL의 `ORDER BY id ASC`에 해당한다. 이름·위치를 저장하는 수정 로직은 그대로 사용한다.

### 확인 결과

- 리포지토리의 정렬 메서드와 서비스 호출이 실제 코드에 반영된 것을 확인했다.
- Java 17의 `compileJava` 검사를 통과했다.
- 사용자가 화면에서 수정 후 순서가 유지되고 ID 오름차순으로 표시되는 것을 수동 확인했다.
- 자동화된 화면 클릭이나 API 수정·재조회 검증은 이번 정렬 변경에서 수행하지 않았다.

현재 설정은 `ddl-auto=create`이므로 서버를 재실행하면 테스트 데이터가 초기화된다. 실행 후 테스트 데이터를 준비한다.

### 배운 점

전체 목록을 조회한다는 것과 원하는 순서로 조회한다는 것은 별개다. 화면에 일정한 순서가 필요하면 DB의 반환 순서에 기대지 않고 조회 코드에 정렬 기준을 명시해야 한다.

### 지원내역에도 같은 해결 방법 적용

지원내역 수정 화면을 테스트하면서 수정한 지원내역의 위치도 바뀌는 문제가 발견됐다. 회사 목록에서 확인한 원인을 바탕으로, 회사별 지원내역 조회에도 ID 오름차순 정렬을 적용했다.

```java
// JobApplicationRepository
List<JobApplication> findByCompanyIdOrderByIdAsc(Long companyId);

// JobApplicationService.index(): 기존 findByCompanyId() 호출을 교체
return jobApplicationRepository.findByCompanyIdOrderByIdAsc(companyId)
        .stream()
        .map(jobApplication -> createJobApplicationDto(jobApplication))
        .collect(Collectors.toList());
```

`CompanyId` 조건은 유지해 해당 회사의 지원내역만 가져오고, `OrderByIdAsc`로 그 목록의 순서를 정한다. 회사 전체 조회와 특정 회사의 지원내역 조회는 범위가 다르므로 조회 조건을 없애지 않았다.

리포지토리와 서비스의 연결, Java 17 컴파일 검사를 확인했다. 사용자가 화면에서 수정 동작과 순서 유지를 수동 확인했다. 이번에도 자동화된 화면 클릭이나 API 수정·재조회 검증은 수행하지 않았다.
