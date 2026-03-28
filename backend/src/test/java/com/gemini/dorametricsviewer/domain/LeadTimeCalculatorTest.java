package com.gemini.dorametricsviewer.domain;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeadTimeCalculatorTest {

    private final LeadTimeCalculator calculator = new LeadTimeCalculator();

    private static final Instant T = Instant.parse("2026-01-20T08:00:00Z");

    // -------------------------------------------------------------------------
    // ExactMatchStrategy (original test, preserved)
    // -------------------------------------------------------------------------

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

        Duration result = calculator.calculate(List.of(change), List.of(deployment));

        assertEquals(Duration.ofHours(2), result);
    }

    // -------------------------------------------------------------------------
    // Empty-input edge cases
    // -------------------------------------------------------------------------

    @Test
    void givenNoChanges_whenCalculate_thenReturnsZero() {
        Deployment deployment = new Deployment(
            "d1", "http://repo.com", "sha1", T, T.plusSeconds(3600), "prod", "SUCCESS", null);

        Duration result = calculator.calculate(List.of(), List.of(deployment));

        assertEquals(Duration.ZERO, result);
    }

    @Test
    void givenNoDeployments_whenCalculate_thenReturnsZero() {
        Change change = new Change(
            "c1", "http://repo.com", "sha1", T, T.plusSeconds(3600), "author");

        Duration result = calculator.calculate(List.of(change), List.of());

        assertEquals(Duration.ZERO, result);
    }

    @Test
    void givenNoStrategyMatchesAnyChange_whenCalculate_thenReturnsZero() {
        // Change has no SHA match, no /pr/ in id, and deployment is deployed BEFORE mergedAt
        Change change = new Change(
            "owner/repo/issues/99", "http://repo.com", "sha-no-match",
            T, T.plusSeconds(3600), "author");

        // deployedAt is BEFORE mergedAt, so TimeWindowStrategy won't pick it up
        Deployment deployment = new Deployment(
            "d1", "http://repo.com", "sha-different",
            T.minusSeconds(7200), T.minusSeconds(3600), "prod", "SUCCESS", null);

        Duration result = calculator.calculate(List.of(change), List.of(deployment));

        assertEquals(Duration.ZERO, result);
    }

    // -------------------------------------------------------------------------
    // ReleaseBodyStrategy
    // -------------------------------------------------------------------------

    @Test
    void givenPrNumberInReleaseBody_whenCalculate_thenUsesReleaseBodyStrategy() {
        // SHA does NOT match, so ExactMatchStrategy is skipped.
        // Change id contains /pr/42 and deployment description contains #42.
        Change change = new Change(
            "owner/repo/pr/42", "http://repo.com", "sha-unmatched",
            T, T.plusSeconds(1800), "author");

        Deployment deployment = new Deployment(
            "d1", "http://repo.com", "sha-release",
            T.plusSeconds(7200), T.plusSeconds(10800), "prod", "SUCCESS",
            "## What's changed\n- Fix bug #42\n- Performance improvement");

        // lead time = deployedAt - change.createdAt = T+3h - T = 3 hours
        Duration result = calculator.calculate(List.of(change), List.of(deployment));

        assertEquals(Duration.ofHours(3), result);
    }

    @Test
    void givenPullUrlInReleaseBody_whenCalculate_thenUsesReleaseBodyStrategy() {
        Change change = new Change(
            "owner/repo/pr/77", "http://repo.com", "sha-unmatched",
            T, T.plusSeconds(1800), "author");

        Deployment deployment = new Deployment(
            "d1", "http://repo.com", "sha-release",
            T.plusSeconds(3600), T.plusSeconds(7200), "prod", "SUCCESS",
            "Includes https://github.com/owner/repo/pull/77");

        // lead time = T+2h - T = 2 hours
        Duration result = calculator.calculate(List.of(change), List.of(deployment));

        assertEquals(Duration.ofHours(2), result);
    }

    // -------------------------------------------------------------------------
    // TimeWindowStrategy
    // -------------------------------------------------------------------------

    @Test
    void givenNoShaOrBodyMatch_whenDeploymentAfterMerge_thenUsesTimeWindowStrategy() {
        // Change has no /pr/ in id (so ReleaseBodyStrategy extracts null prNumber).
        // Deployment deployedAt is after change mergedAt → TimeWindowStrategy picks it up.
        Instant mergedAt = T.plusSeconds(3600);       // T + 1h
        Instant deployedAt = T.plusSeconds(7200);     // T + 2h

        Change change = new Change(
            "owner/repo/issues/55", "http://repo.com", "sha-x",
            T, mergedAt, "author");

        Deployment deployment = new Deployment(
            "d1", "http://repo.com", "sha-y",
            deployedAt.minusSeconds(600), deployedAt, "prod", "SUCCESS", null);

        // lead time = deployedAt - change.createdAt = T+2h - T = 2 hours
        Duration result = calculator.calculate(List.of(change), List.of(deployment));

        assertEquals(Duration.ofHours(2), result);
    }

    @Test
    void givenMultipleDeployments_whenTimeWindowStrategy_thenPicksEarliest() {
        Instant mergedAt = T.plusSeconds(3600);         // T + 1h

        Change change = new Change(
            "owner/repo/issues/10", "http://repo.com", "sha-x",
            T, mergedAt, "author");

        Deployment early = new Deployment(
            "d-early", "http://repo.com", "sha-y",
            T.plusSeconds(5000), T.plusSeconds(5400), "prod", "SUCCESS", null); // T+1.5h

        Deployment late = new Deployment(
            "d-late", "http://repo.com", "sha-z",
            T.plusSeconds(9000), T.plusSeconds(10800), "prod", "SUCCESS", null); // T+3h

        // Should match 'early' (T+1.5h) → lead time = T+1.5h - T = 1.5h
        Duration result = calculator.calculate(List.of(change), List.of(late, early));

        assertEquals(Duration.ofMinutes(90), result);
    }

    // -------------------------------------------------------------------------
    // Multi-change average across strategies
    // -------------------------------------------------------------------------

    @Test
    void givenMultipleChangesMatchedByDifferentStrategies_whenCalculate_thenReturnsCorrectAverage() {
        // change1: matched by ExactMatchStrategy → lead time 2h
        Change change1 = new Change(
            "owner/repo/pr/1", "http://repo.com", "sha1",
            T, T.plusSeconds(3600), "author1");

        Deployment deployment1 = new Deployment(
            "d1", "http://repo.com", "sha1",
            T.plusSeconds(5400), T.plusSeconds(7200), "prod", "SUCCESS", null); // T+2h

        // change2: SHA doesn't match any deployment, matched by ReleaseBodyStrategy → lead time 4h
        Change change2 = new Change(
            "owner/repo/pr/99", "http://repo.com", "sha2",
            T, T.plusSeconds(3600), "author2");

        Deployment deployment2 = new Deployment(
            "d2", "http://repo.com", "sha-other",
            T.plusSeconds(12600), T.plusSeconds(14400), "prod", "SUCCESS",
            "Release containing #99"); // T+4h

        // average = (2h + 4h) / 2 = 3h
        Duration result = calculator.calculate(
            List.of(change1, change2),
            List.of(deployment1, deployment2));

        assertEquals(Duration.ofHours(3), result);
    }
}
