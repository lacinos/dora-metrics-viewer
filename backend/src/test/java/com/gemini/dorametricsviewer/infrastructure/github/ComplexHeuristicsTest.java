package com.gemini.dorametricsviewer.infrastructure.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gemini.dorametricsviewer.domain.LeadTimeCalculator;
import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexHeuristicsTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void givenComplexRealWorldRepo_whenCalculateLeadTimeWithRobustLogic_thenReturnsMetrics() throws IOException {
        // 1. Load Real Data from JSON
        List<GitHubReleaseDTO> releaseDTOs = loadDeployments("backend/src/test/resources/datasets/vscode/deployments.json");
        List<GitHubPullRequestDTO> prDTOs = loadChanges("backend/src/test/resources/datasets/vscode/changes.json");
        
        // 2. Map to Domain Objects
        List<Deployment> deployments = releaseDTOs.stream()
                .map(r -> new Deployment(
                        "vscode/release/" + r.id(),
                        "https://github.com/microsoft/vscode",
                        r.targetCommitish(),
                        r.createdAt(),
                        r.publishedAt(),
                        "production",
                        "SUCCESS",
                        r.body()
                ))
                .collect(Collectors.toList());

        List<Change> changes = prDTOs.stream()
                .map(pr -> new Change(
                        "vscode/pr/" + pr.number(),
                        "https://github.com/microsoft/vscode",
                        pr.mergeCommitSha(),
                        pr.createdAt(),
                        pr.mergedAt(),
                        pr.user() != null ? pr.user().login() : "unknown"
                ))
                .collect(Collectors.toList());

        // 3. Run LeadTimeCalculator
        LeadTimeCalculator calculator = new LeadTimeCalculator();
        Duration leadTime = calculator.calculate(changes, deployments);

        // 4. Assert Success (Robustness)
        // With Heuristics (TimeWindow and Body Parsing), we expect to find matches.
        System.out.println("Calculated Lead Time: " + leadTime);
        System.out.println("Deployments: " + deployments.size());
        System.out.println("Changes: " + changes.size());
        
        // Assert that we found at least some matches, resulting in a positive lead time.
        // VS Code PRs usually take days/weeks.
        assertThat(leadTime).isGreaterThan(Duration.ZERO);
    }

    private List<GitHubReleaseDTO> loadDeployments(String path) throws IOException {
         // Adjust path if running from root
        if (!Files.exists(Paths.get(path)) && Files.exists(Paths.get("src"))) {
            path = path.replace("backend/", "");
        }
        return objectMapper.readValue(Files.readString(Paths.get(path)), new TypeReference<>() {});
    }

    private List<GitHubPullRequestDTO> loadChanges(String path) throws IOException {
        // Adjust path if running from root
        if (!Files.exists(Paths.get(path)) && Files.exists(Paths.get("src"))) {
            path = path.replace("backend/", "");
        }
        return objectMapper.readValue(Files.readString(Paths.get(path)), new TypeReference<>() {});
    }
}
