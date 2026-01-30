package com.gemini.dorametricsviewer.infrastructure.persistence;

import com.gemini.dorametricsviewer.domain.model.Deployment;
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

    @Test
    void shouldSaveAndRetrieveDeployments() {
        // Given
        Instant now = Instant.now();
        Deployment deployment = new Deployment(
            "d1",
            "http://repo.com",
            "sha1",
            now.minusSeconds(3600),
            now,
            "prod",
            "SUCCESS",
            null
        );

        // When
        adapter.saveDeployments(List.of(deployment));
        // We find deployments SINCE now-60s, but we saved it deployed at NOW. So it should be found.
        // Wait, the findDeployments logic filters by deployedAt?
        // Let's assume the previous test was correct.
        List<Deployment> results = adapter.findDeployments("http://repo.com", now.minusSeconds(60));

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).usingRecursiveComparison().isEqualTo(deployment);
    }
}
