package com.gemini.dorametricsviewer.infrastructure.web;

import com.gemini.dorametricsviewer.application.DoraMetricsService;
import com.gemini.dorametricsviewer.domain.TimeWindow;
import com.gemini.dorametricsviewer.infrastructure.web.dto.GraphQLDoraMetricsResult;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;

@Controller
public class DoraMetricsGraphQLController {

    private final DoraMetricsService doraMetricsService;

    public DoraMetricsGraphQLController(DoraMetricsService doraMetricsService) {
        this.doraMetricsService = doraMetricsService;
    }

    @MutationMapping
    public GraphQLDoraMetricsResult scanRepository(
            @Argument String repoUrl,
            @Argument Map<String, String> timeWindow) {
        TimeWindow window = new TimeWindow(
                Instant.parse(timeWindow.get("start")),
                Instant.parse(timeWindow.get("end"))
        );
        return GraphQLDoraMetricsResult.from(
                doraMetricsService.calculateMetrics(repoUrl, window)
        );
    }
}
