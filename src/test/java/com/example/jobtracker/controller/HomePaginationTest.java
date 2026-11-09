package com.example.jobtracker.controller;

import com.example.jobtracker.entity.Company;
import com.example.jobtracker.repository.CompanyRepository;
import com.example.jobtracker.service.CompanyService;
import com.example.jobtracker.service.JobApplicationService;
import com.samskivert.mustache.Mustache;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// AI가 사용자 요청으로 작성한 검증 보조. 실제 서비스·컨트롤러·템플릿을 사용하고 DB 조회만 대역으로 준비한다.
class HomePaginationTest {
    @ParameterizedTest(name = "전체 {0}페이지, 요청 page={1}")
    @MethodSource("navigationCases")
    void rendersExpectedNumberGroupsAndArrowDestinations(
            int totalPages, int requestPage, String expectedLinks) throws Exception {
        CompanyRepository repository = mock(CompanyRepository.class);
        CompanyService companyService = new CompanyService();
        ReflectionTestUtils.setField(companyService, "companyRepository", repository);
        JobApplicationService applicationService = mock(JobApplicationService.class);
        when(applicationService.index(anyLong())).thenReturn(List.of());

        // 마지막 페이지에 회사 1개가 남도록, 지정한 전체 페이지 수에 맞는 조회 결과를 준비한다.
        int companyCount = totalPages == 0 ? 0 : (totalPages - 1) * 5 + 1;
        PageRequest query = PageRequest.of(requestPage, 5, Sort.by("id").descending());
        List<Company> rows = new ArrayList<>();
        int rowCount = Math.min(5, Math.max(0, companyCount - requestPage * 5));
        for (int i = 0; i < rowCount; i++) {
            long id = companyCount - requestPage * 5L - i;
            rows.add(new Company(id, "테스트 회사" + id, "서울"));
        }
        when(repository.findAll(query)).thenReturn(new PageImpl<>(rows, query, companyCount));

        HomeController controller = new HomeController();
        ReflectionTestUtils.setField(controller, "companyService", companyService);
        ReflectionTestUtils.setField(controller, "jobApplicationService", applicationService);
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("home/home", controller.index(requestPage, model));
        verify(repository).findAll(query);
        assertEquals(totalPages, model.get("companiesTotalPages"));
        assertEquals(requestPage + 1, model.get("currentPage"));
        assertEquals(totalPages > 0, model.get("hasPages"));

        String template = Files.readString(Path.of("src/main/resources/templates/home/home.mustache"));
        String html = Mustache.compiler().compile(template).execute(model);
        // 회사가 없으면 목록 안내만 남고, 페이지 문구를 포함한 nav 영역은 숨겨진다.
        assertEquals(totalPages > 0, html.contains("<nav class=\"pagination\""));
        if (totalPages == 0) {
            org.junit.jupiter.api.Assertions.assertTrue(html.contains("등록된 회사가 없습니다."));
            org.junit.jupiter.api.Assertions.assertFalse(html.contains("현재 1페이지 / 전체 0페이지"));
        }
        // 실제 템플릿과 CSS로 만든 DB 없는 배치 확인용 미리보기. build 폴더는 Git에서 제외된다.
        if (totalPages == 13 && requestPage == 7) {
            Path preview = Path.of("build/reports/pagination-preview");
            Files.createDirectories(preview);
            Files.writeString(preview.resolve("index.html"), html.replace("/css/jobtracker.css", "./jobtracker.css"));
            Files.copy(Path.of("src/main/resources/static/css/jobtracker.css"), preview.resolve("jobtracker.css"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        var matcher = Pattern.compile("<a\\s+href=\"/\\?page=(-?\\d+)\"[^>]*>([^<]*)</a>").matcher(html);
        List<String> actualLinks = new ArrayList<>();
        List<String> currentLinks = new ArrayList<>();
        while (matcher.find()) {
            String label = matcher.group(2).replace("&lt;", "<").replace("&gt;", ">").trim();
            actualLinks.add(label + ":" + matcher.group(1));
            if (matcher.group().contains("aria-current=\"page\"")) {
                currentLinks.add(label + ":" + matcher.group(1));
                org.junit.jupiter.api.Assertions.assertTrue(matcher.group().contains("is-current"));
            }
        }
        // 기대 결과는 요구사항의 구체적인 예시로 적고, 링크 순서·표시 조건·이동 주소를 검증한다.
        assertEquals(expectedLinks, String.join("|", actualLinks));
        // 빈 목록을 제외하면 현재 번호 하나에만 강조 표시가 있어야 한다.
        assertEquals(totalPages == 0 ? List.of() : List.of((requestPage + 1) + ":" + requestPage), currentLinks);
    }

    static Stream<Arguments> navigationCases() {
        return Stream.of(
                Arguments.of(0, 0, ""),
                Arguments.of(0, 999, ""),
                Arguments.of(1, 0, "1:0"),
                Arguments.of(5, 0, "1:0|2:1|3:2|4:3|5:4|>>:4"),
                Arguments.of(5, 4, "<<:0|1:0|2:1|3:2|4:3|5:4"),
                Arguments.of(6, 2, "<<:0|1:0|2:1|3:2|4:3|5:4|>:5|>>:5"),
                Arguments.of(6, 5, "<<:0|<:0|6:5"),
                Arguments.of(13, 7, "<<:0|<:0|6:5|7:6|8:7|9:8|10:9|>:10|>>:12"),
                Arguments.of(13, 10, "<<:0|<:5|11:10|12:11|13:12|>>:12"),
                Arguments.of(13, 12, "<<:0|<:5|11:10|12:11|13:12"),
                Arguments.of(15, 9, "<<:0|<:0|6:5|7:6|8:7|9:8|10:9|>:10|>>:14"),
                Arguments.of(15, 14, "<<:0|<:5|11:10|12:11|13:12|14:13|15:14"),
                Arguments.of(16, 12, "<<:0|<:5|11:10|12:11|13:12|14:13|15:14|>:15|>>:15"),
                Arguments.of(16, 15, "<<:0|<:10|16:15")
        );
    }

    @ParameterizedTest(name = "회사 {0}개, 요청 page={1} → {2}")
    @MethodSource("redirectCases")
    void redirectsInvalidPageWithoutRendering(int companyCount, int requestPage, String expectedView) {
        CompanyRepository repository = mock(CompanyRepository.class);
        CompanyService companyService = new CompanyService();
        ReflectionTestUtils.setField(companyService, "companyRepository", repository);
        JobApplicationService applicationService = mock(JobApplicationService.class);
        if (requestPage >= 0) {
            PageRequest query = PageRequest.of(requestPage, 5, Sort.by("id").descending());
            when(repository.findAll(query)).thenReturn(new PageImpl<>(List.of(), query, companyCount));
        }
        HomeController controller = new HomeController();
        ReflectionTestUtils.setField(controller, "companyService", companyService);
        ReflectionTestUtils.setField(controller, "jobApplicationService", applicationService);

        assertEquals(expectedView, controller.index(requestPage, new ExtendedModelMap()));
        // 음수는 조회 전에 거절하고, 이동 응답에서는 지원내역 조회도 하지 않는다.
        if (requestPage < 0) {
            verifyNoInteractions(repository);
        } else {
            verify(repository).findAll(PageRequest.of(requestPage, 5, Sort.by("id").descending()));
        }
        verifyNoInteractions(applicationService);
    }

    static Stream<Arguments> redirectCases() {
        return Stream.of(
                Arguments.of(26, -999, "redirect:/?page=0"),
                Arguments.of(26, -1, "redirect:/?page=0"),
                Arguments.of(26, 999, "redirect:/?page=5"),
                Arguments.of(25, 5, "redirect:/?page=4"),
                Arguments.of(24, 5, "redirect:/?page=4")
        );
    }
}
