package com.example.jobtracker.exception;

import com.example.jobtracker.controller.CompanyApiController;
import com.example.jobtracker.controller.JobApplicationApiController;
import com.example.jobtracker.dto.CompanyRequestDto;
import com.example.jobtracker.dto.CompanyResponseDto;
import com.example.jobtracker.entity.Company;
import com.example.jobtracker.repository.CompanyRepository;
import com.example.jobtracker.service.CompanyService;
import com.example.jobtracker.service.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {
    private MockMvc mockMvc;
    private CompanyService companyService;
    private JobApplicationService jobApplicationService;

    // 실제 컨트롤러와 검증을 사용하되 서비스는 대역으로 준비해 DB 없이 요청을 검사한다.
    @BeforeEach
    void setUp() {
        companyService = mock(CompanyService.class);
        jobApplicationService = mock(JobApplicationService.class);
        CompanyApiController companyController = new CompanyApiController();
        JobApplicationApiController applicationController = new JobApplicationApiController();
        ReflectionTestUtils.setField(companyController, "companyService", companyService);
        ReflectionTestUtils.setField(applicationController, "jobApplicationService", jobApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(companyController, applicationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // null, 빈 문자열, 공백 회사명은 400과 안내 문장을 반환하고 서비스를 실행하지 않는다.
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void invalidCompanyNameReturnsMessageWithoutCallingService(String name) throws Exception {
        String nameJson = name == null ? "null" : "\"" + name + "\"";
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":" + nameJson + ",\"location\":\"서울\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("회사명은 필수입니다."));
        verifyNoInteractions(companyService, jobApplicationService);
    }

    // 지원 날짜 누락도 다른 컨트롤러에서 같은 오류 응답 형식으로 처리한다.
    @Test
    void missingApplicationDateReturnsMessageWithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/companies/1/job-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"백엔드 개발자\",\"appliedAt\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("지원 날짜는 필수입니다."));
        verifyNoInteractions(companyService, jobApplicationService);
    }

    // 정상 입력은 기존처럼 서비스를 호출하고 201과 등록 응답을 반환한다.
    @Test
    void validCompanyCreationKeepsSuccessfulResponse() throws Exception {
        when(companyService.create(any(CompanyRequestDto.class)))
                .thenReturn(new CompanyResponseDto(7L, "테스트 회사", "서울"));
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"테스트 회사\",\"location\":\"서울\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("테스트 회사"));
        verify(companyService).create(any(CompanyRequestDto.class));
    }

    // 실제 서비스가 중복을 발견하면 저장하지 않고, 예외 처리기를 거쳐 409와 안내 문장을 반환한다.
    @Test
    void duplicateCompanyReturnsConflictWithoutSaving() throws Exception {
        CompanyRepository repository = mock(CompanyRepository.class);
        when(repository.existsByName("테스트 회사")).thenReturn(true);
        CompanyService realService = new CompanyService();
        ReflectionTestUtils.setField(realService, "companyRepository", repository);
        CompanyApiController controller = new CompanyApiController();
        ReflectionTestUtils.setField(controller, "companyService", realService);
        MockMvc duplicateMockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        duplicateMockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"테스트 회사\",\"location\":\"서울\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 등록된 회사입니다."));
        verify(repository).existsByName("테스트 회사");
        verify(repository, never()).save(any(Company.class));
    }

    // 입력 검증을 통과한 뒤 대상이 없는 경우의 기존 404 처리는 유지한다.
    @Test
    void missingCompanyUpdateKeepsNotFoundResponse() throws Exception {
        mockMvc.perform(patch("/api/companies/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"테스트 회사\",\"location\":\"서울\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
        verify(companyService).update(eq(99L), any(CompanyRequestDto.class));
    }
}
