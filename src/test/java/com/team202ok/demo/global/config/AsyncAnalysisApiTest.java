package com.team202ok.demo.global.config;

import com.team202ok.demo.domain.rabbitmq.AnalysisJobService;
import com.team202ok.demo.global.auth.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AsyncAnalysisApiTest {
    @Autowired MockMvc mvc;
    @Autowired RequestMappingHandlerMapping mappings;
    @MockitoBean AnalysisJobService jobs;
    @MockitoBean JwtTokenProvider tokens;

    @Test void acceptsAtTheOnlyAnalysisSubmissionRoute() throws Exception {
        when(tokens.validateToken("test-token")).thenReturn(true);
        when(tokens.isAccessToken("test-token")).thenReturn(true);
        when(tokens.getUserId("test-token")).thenReturn(1L);
        when(jobs.submit(any(), eq(1L))).thenReturn(new AnalysisJobService.JobView("job-123", "QUEUED", null, null));
        mvc.perform(multipart("/api/ai/analysis")
                        .file(new MockMultipartFile("image", "test.png", "image/png", new byte[]{1}))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", "/api/ai/analysis/job-123"))
                .andExpect(jsonPath("$.jobId").value("job-123"))
                .andExpect(jsonPath("$.status").value("QUEUED"));
        var postPaths = mappings.getHandlerMethods().keySet().stream()
                .filter(m -> m.getMethodsCondition().getMethods().contains(RequestMethod.POST))
                .flatMap(m -> m.getPatternValues().stream()).toList();
        assertThat(postPaths).contains("/api/ai/analysis")
                .doesNotContain("/api/scans", "/api/scans/jobs");
    }
}
