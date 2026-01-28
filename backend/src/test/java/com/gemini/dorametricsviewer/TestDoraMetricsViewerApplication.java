package com.gemini.dorametricsviewer;

import org.springframework.boot.SpringApplication;

public class TestDoraMetricsViewerApplication {

    public static void main(String[] args) {
        SpringApplication.from(DoraMetricsViewerApplication::main)
                .with(TestConfig.class)
                .run(args);
    }
}
