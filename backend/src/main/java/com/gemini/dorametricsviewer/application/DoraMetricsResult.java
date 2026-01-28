package com.gemini.dorametricsviewer.application;

import java.time.Duration;

public record DoraMetricsResult(
    Duration leadTimeForChanges,
    double deploymentFrequency, // Deployments per day
    double changeFailureRate,   // Percentage (0-100)
    Duration timeToRestoreService
) {
}