package com.gemini.dorametricsviewer.infrastructure.web.dto;

import com.gemini.dorametricsviewer.application.DoraMetricsResult;

public record GraphQLDoraMetricsResult(
        String leadTimeForChanges,
        double deploymentFrequency,
        double changeFailureRate,
        String timeToRestoreService
) {
    public static GraphQLDoraMetricsResult from(DoraMetricsResult result) {
        return new GraphQLDoraMetricsResult(
                result.leadTimeForChanges().toString(),
                result.deploymentFrequency(),
                result.changeFailureRate(),
                result.timeToRestoreService().toString()
        );
    }
}
