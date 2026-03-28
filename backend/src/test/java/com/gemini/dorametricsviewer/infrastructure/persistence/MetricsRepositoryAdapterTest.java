package com.gemini.dorametricsviewer.infrastructure.persistence;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.model.Incident;
import com.gemini.dorametricsviewer.infrastructure.persistence.mapper.MetricsMapper;
import com.gemini.dorametricsviewer.infrastructure.persistence.repository.SpringDataChangeRepository;
import com.gemini.dorametricsviewer.infrastructure.persistence.repository.SpringDataDeploymentRepository;
import com.gemini.dorametricsviewer.infrastructure.persistence.repository.SpringDataIncidentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({MetricsRepositoryAdapter.class, MetricsMapper.class})
class MetricsRepositoryAdapterTest {

    @Autowired
    private MetricsRepositoryAdapter adapter;

    @Autowired
    private SpringDataDeploymentRepository deploymentRepository;

    @Autowired
    private SpringDataChangeRepository changeRepository;

    @Autowired
    private SpringDataIncidentRepository incidentRepository;

    private static final String REPO_URL = "http://repo.com";

    // -------------------------------------------------------------------------
    // Save & retrieve — all three entity types
    // -------------------------------------------------------------------------

    @Test
    void shouldSaveAndRetrieveDeployments() {
        Instant now = Instant.now();
        Deployment deployment = new Deployment(
            "d1", REPO_URL, "sha1",
            now.minusSeconds(3600), now,
            "prod", "SUCCESS", null
        );

        adapter.saveDeployments(List.of(deployment));
        List<Deployment> results = adapter.findDeployments(REPO_URL, now.minusSeconds(60));

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).usingRecursiveComparison().isEqualTo(deployment);
    }

    @Test
    void shouldSaveAndRetrieveChanges() {
        Instant now = Instant.now();
        Change change = new Change(
            "c1", REPO_URL, "sha1",
            now.minusSeconds(7200), now.minusSeconds(3600),
            "author1"
        );

        adapter.saveChanges(List.of(change));
        List<Change> results = adapter.findChanges(REPO_URL, now.minusSeconds(7200));

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).usingRecursiveComparison().isEqualTo(change);
    }

    @Test
    void shouldSaveAndRetrieveIncidents() {
        Instant now = Instant.now();
        Incident incident = new Incident(
            "i1", REPO_URL,
            now.minusSeconds(3600), now,
            "high", "Production outage"
        );

        adapter.saveIncidents(List.of(incident));
        List<Incident> results = adapter.findIncidents(REPO_URL, now.minusSeconds(7200));

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).usingRecursiveComparison().isEqualTo(incident);
    }

    // -------------------------------------------------------------------------
    // Bounded time-window queries
    // -------------------------------------------------------------------------

    @Test
    void shouldOnlyReturnDeploymentsWithinBoundedTimeWindow() {
        Instant windowStart = Instant.parse("2026-01-10T00:00:00Z");
        Instant windowEnd   = Instant.parse("2026-01-20T00:00:00Z");

        Deployment before = new Deployment(
            "d-before", REPO_URL, "sha-b",
            Instant.parse("2026-01-05T00:00:00Z"), Instant.parse("2026-01-05T00:00:00Z"),
            "prod", "SUCCESS", null);

        Deployment inside = new Deployment(
            "d-inside", REPO_URL, "sha-i",
            Instant.parse("2026-01-15T00:00:00Z"), Instant.parse("2026-01-15T00:00:00Z"),
            "prod", "SUCCESS", null);

        Deployment after = new Deployment(
            "d-after", REPO_URL, "sha-a",
            Instant.parse("2026-01-25T00:00:00Z"), Instant.parse("2026-01-25T00:00:00Z"),
            "prod", "SUCCESS", null);

        adapter.saveDeployments(List.of(before, inside, after));

        List<Deployment> results = adapter.findDeployments(REPO_URL, windowStart, windowEnd);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo("d-inside");
    }

    @Test
    void shouldOnlyReturnChangesWithinBoundedTimeWindow() {
        Instant windowStart = Instant.parse("2026-01-10T00:00:00Z");
        Instant windowEnd   = Instant.parse("2026-01-20T00:00:00Z");

        Change before = new Change(
            "c-before", REPO_URL, "sha-b",
            Instant.parse("2026-01-04T00:00:00Z"), Instant.parse("2026-01-05T00:00:00Z"),
            "author");

        Change inside = new Change(
            "c-inside", REPO_URL, "sha-i",
            Instant.parse("2026-01-14T00:00:00Z"), Instant.parse("2026-01-15T00:00:00Z"),
            "author");

        Change after = new Change(
            "c-after", REPO_URL, "sha-a",
            Instant.parse("2026-01-24T00:00:00Z"), Instant.parse("2026-01-25T00:00:00Z"),
            "author");

        adapter.saveChanges(List.of(before, inside, after));

        List<Change> results = adapter.findChanges(REPO_URL, windowStart, windowEnd);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo("c-inside");
    }

    @Test
    void shouldOnlyReturnIncidentsWithinBoundedTimeWindow() {
        Instant windowStart = Instant.parse("2026-01-10T00:00:00Z");
        Instant windowEnd   = Instant.parse("2026-01-20T00:00:00Z");

        Incident before = new Incident(
            "i-before", REPO_URL,
            Instant.parse("2026-01-05T00:00:00Z"), Instant.parse("2026-01-06T00:00:00Z"),
            "low", "minor");

        Incident inside = new Incident(
            "i-inside", REPO_URL,
            Instant.parse("2026-01-15T00:00:00Z"), Instant.parse("2026-01-16T00:00:00Z"),
            "high", "major");

        Incident after = new Incident(
            "i-after", REPO_URL,
            Instant.parse("2026-01-25T00:00:00Z"), Instant.parse("2026-01-26T00:00:00Z"),
            "low", "minor");

        adapter.saveIncidents(List.of(before, inside, after));

        List<Incident> results = adapter.findIncidents(REPO_URL, windowStart, windowEnd);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo("i-inside");
    }
}
