package com.gemini.dorametricsviewer.infrastructure.web.dto;

import com.gemini.dorametricsviewer.domain.TimeWindow;

public record ScanRequest(String repoUrl, TimeWindow timeWindow) {
}
