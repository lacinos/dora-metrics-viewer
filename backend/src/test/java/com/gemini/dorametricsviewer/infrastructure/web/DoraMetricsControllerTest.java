package com.gemini.dorametricsviewer.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemini.dorametricsviewer.application.DoraMetricsResult;
import com.gemini.dorametricsviewer.application.DoraMetricsService;
import com.gemini.dorametricsviewer.domain.TimeWindow;
import com.gemini.dorametricsviewer.infrastructure.web.dto.ScanRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoraMetricsController.class)
class DoraMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoraMetricsService doraMetricsService;

    // -------------------------------------------------------------------------
    // Happy-path test (original)
    // -------------------------------------------------------------------------

    @Test
    void scan_shouldReturnMetrics() throws Exception {
        String repoUrl = "https://github.com/owner/repo";
        TimeWindow timeWindow = new TimeWindow(Instant.now().minusSeconds(3600), Instant.now());
        ScanRequest request = new ScanRequest(repoUrl, timeWindow);

        DoraMetricsResult expectedResult = new DoraMetricsResult(
            Duration.ofHours(2),
            1.5,
            20.0,
            Duration.ofMinutes(30)
        );

        when(doraMetricsService.calculateMetrics(eq(repoUrl), any(TimeWindow.class)))
                .thenReturn(expectedResult);

        mockMvc.perform(post("/api/metrics/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadTimeForChanges").exists())
                .andExpect(jsonPath("$.deploymentFrequency").value(1.5))
                .andExpect(jsonPath("$.changeFailureRate").value(20.0));
    }

    // -------------------------------------------------------------------------
    // Exception-handler tests
    // -------------------------------------------------------------------------

    @Test
    void scan_shouldReturn400_whenServiceThrowsIllegalArgumentException() throws Exception {
        String repoUrl = "https://github.com/owner/repo";
        TimeWindow timeWindow = new TimeWindow(Instant.now().minusSeconds(3600), Instant.now());
        ScanRequest request = new ScanRequest(repoUrl, timeWindow);

        when(doraMetricsService.calculateMetrics(any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid repository URL"));

        mockMvc.perform(post("/api/metrics/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid repository URL"));
    }

    @Test
    void scan_shouldReturn500_whenServiceThrowsGenericException() throws Exception {
        String repoUrl = "https://github.com/owner/repo";
        TimeWindow timeWindow = new TimeWindow(Instant.now().minusSeconds(3600), Instant.now());
        ScanRequest request = new ScanRequest(repoUrl, timeWindow);

        when(doraMetricsService.calculateMetrics(any(), any()))
                .thenThrow(new RuntimeException("GitHub API unreachable"));

        mockMvc.perform(post("/api/metrics/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("GitHub API unreachable")));
    }
}
