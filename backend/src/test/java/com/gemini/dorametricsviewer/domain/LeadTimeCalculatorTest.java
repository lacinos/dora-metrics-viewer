package com.gemini.dorametricsviewer.domain;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeadTimeCalculatorTest {

    @Test
    void givenChangesAndDeployments_whenCalculate_thenReturnsCorrectAverage() {
        Instant changeTime = Instant.parse("2026-01-20T10:00:00Z");
        Instant deployTime = Instant.parse("2026-01-20T12:00:00Z");

        Change change = new Change(
            "c1",
            "http://repo.com",
            "sha1",
            changeTime,
            changeTime.plusSeconds(60),
            "author"
        );

        Deployment deployment = new Deployment(
            "d1",
            "http://repo.com",
            "sha1",
            deployTime.minusSeconds(60),
            deployTime,
            "prod",
            "SUCCESS",
            null
        );

        LeadTimeCalculator calculator = new LeadTimeCalculator();
        Duration result = calculator.calculate(List.of(change), List.of(deployment));

        assertEquals(Duration.ofHours(2), result);
    }
}