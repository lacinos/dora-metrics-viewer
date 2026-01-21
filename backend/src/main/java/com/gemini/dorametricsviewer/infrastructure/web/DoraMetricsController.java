package com.gemini.dorametricsviewer.infrastructure.web;

import com.gemini.dorametricsviewer.application.DoraMetricsResult;
import com.gemini.dorametricsviewer.application.DoraMetricsService;
import com.gemini.dorametricsviewer.infrastructure.web.dto.ScanRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Dora Metrics")
public class DoraMetricsController {

    private final DoraMetricsService doraMetricsService;

    public DoraMetricsController(DoraMetricsService doraMetricsService) {
        this.doraMetricsService = doraMetricsService;
    }

    @PostMapping("/scan")
    public ResponseEntity<DoraMetricsResult> scan(@RequestBody ScanRequest request) {
        DoraMetricsResult result = doraMetricsService.calculateMetrics(request.repoUrl(), request.timeWindow());
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
    }
}
