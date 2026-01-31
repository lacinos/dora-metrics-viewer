package com.gemini.dorametricsviewer.infrastructure.github;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(GitHubAdapter.class)
@Import(GitHubProperties.class)
class GitHubAdapterTest {

    @Autowired
    private GitHubAdapter adapter;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void fetchDeployments_shouldReturnDeployments() {
        String repoUrl = "https://github.com/owner/repo";
        String responseJson = """
            [
                {
                    "id": 123,
                    "tag_name": "v1.0.0",
                    "target_commitish": "sha123",
                    "created_at": "2023-10-01T10:00:00Z",
                    "published_at": "2023-10-01T12:00:00Z",
                    "html_url": "http://github.com/owner/repo/releases/v1.0.0"
                }
            ]
            """;

        server.expect(requestTo("https://api.github.com/repos/owner/repo/releases"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<Deployment> deployments = adapter.fetchDeployments(repoUrl, Instant.parse("2023-01-01T00:00:00Z"));

        assertThat(deployments).hasSize(1);
        Deployment d = deployments.get(0);
        assertThat(d.id()).isEqualTo("owner/repo/release/123");
        assertThat(d.commitSha()).isEqualTo("sha123");
        assertThat(d.createdAt()).isEqualTo("2023-10-01T10:00:00Z");
        assertThat(d.deployedAt()).isEqualTo("2023-10-01T12:00:00Z");
    }

    @Test
    void fetchChanges_shouldReturnChanges() {
        String repoUrl = "https://github.com/owner/repo";
        String responseJson = """
            [
                {
                    "id": 1001,
                    "number": 42,
                    "merge_commit_sha": "sha456",
                    "created_at": "2023-10-02T10:00:00Z",
                    "merged_at": "2023-10-02T14:00:00Z",
                    "user": { "login": "dev1" },
                    "html_url": "http://github.com/owner/repo/pull/42"
                }
            ]
            """;

        server.expect(requestTo("https://api.github.com/repos/owner/repo/pulls?state=closed&per_page=100&page=1"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<Change> changes = adapter.fetchChanges(repoUrl, Instant.parse("2023-01-01T00:00:00Z"));

        assertThat(changes).hasSize(1);
        Change c = changes.get(0);
        assertThat(c.id()).isEqualTo("owner/repo/pr/42");
        assertThat(c.commitSha()).isEqualTo("sha456");
        assertThat(c.author()).isEqualTo("dev1");
    }
}