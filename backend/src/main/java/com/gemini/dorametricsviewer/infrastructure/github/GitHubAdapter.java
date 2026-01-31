package com.gemini.dorametricsviewer.infrastructure.github;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.model.Incident;
import com.gemini.dorametricsviewer.domain.port.SourceControlPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GitHubAdapter implements SourceControlPort {

    private final RestClient restClient;

    public GitHubAdapter(GitHubProperties properties, RestClient.Builder builder) {
        var clientBuilder = builder.baseUrl(properties.getBaseUrl());
        if (properties.getToken() != null && !properties.getToken().isBlank()) {
            clientBuilder.defaultHeader("Authorization", "Bearer " + properties.getToken());
        }
        this.restClient = clientBuilder.build();
    }

    @Override
    public List<Deployment> fetchDeployments(String repoUrl, Instant since) {
        var repoPath = extractRepoPath(repoUrl);
        List<GitHubReleaseDTO> releases = restClient.get()
                .uri("/repos/" + repoPath + "/releases")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (releases == null) {
            return Collections.emptyList();
        }

        return releases.stream()
                .filter(r -> r.publishedAt() != null && r.publishedAt().isAfter(since))
                .map(r -> new Deployment(
                        repoPath + "/release/" + r.id(),
                        repoUrl,
                        r.targetCommitish(),
                        r.createdAt(),
                        r.publishedAt(),
                        "production",
                        "SUCCESS",
                        r.body()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<Change> fetchChanges(String repoUrl, Instant since) {
        var repoPath = extractRepoPath(repoUrl);
        List<Change> allChanges = new ArrayList<>();
        int page = 1;
        // Fetch up to 8 pages (800 items) to cover enough history for busy repos like vscode
        while (page <= 8) {
            int currentPage = page;
            List<GitHubPullRequestDTO> prs = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/" + repoPath + "/pulls")
                            .queryParam("state", "closed")
                            .queryParam("per_page", 100)
                            .queryParam("page", currentPage)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (prs == null || prs.isEmpty()) {
                break;
            }

            List<Change> pageChanges = prs.stream()
                    .filter(pr -> pr.mergedAt() != null && pr.mergedAt().isAfter(since))
                    .map(pr -> new Change(
                            repoPath + "/pr/" + pr.number(),
                            repoUrl,
                            pr.mergeCommitSha(),
                            pr.createdAt(),
                            pr.mergedAt(),
                            pr.user() != null ? pr.user().login() : "unknown"
                    ))
                    .collect(Collectors.toList());
            
            allChanges.addAll(pageChanges);
            
            // If the last PR in the page is older than 'since', we can stop.
            // Note: This relies on the API returning roughly roughly reverse chronological order.
            // We check the mergedAt of the last item if it exists.
            if (!prs.isEmpty()) {
                GitHubPullRequestDTO lastPr = prs.get(prs.size() - 1);
                if (lastPr.mergedAt() != null && lastPr.mergedAt().isBefore(since)) {
                    break;
                }
                // If created_at is significantly before since, we might also stop, 
                // but mergedAt is the source of truth for the filter.
            }

            if (prs.size() < 100) {
                break;
            }
            page++;
        }

        return allChanges;
    }

    @Override
    public List<Incident> fetchIncidents(String repoUrl, Instant since) {
        return Collections.emptyList();
    }

    private String extractRepoPath(String repoUrl) {
        String cleanUrl = repoUrl;
        if (cleanUrl.endsWith(".git")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 4);
        }
        if (cleanUrl.startsWith("https://github.com/")) {
            return cleanUrl.substring("https://github.com/".length());
        }
        return cleanUrl;
    }
}