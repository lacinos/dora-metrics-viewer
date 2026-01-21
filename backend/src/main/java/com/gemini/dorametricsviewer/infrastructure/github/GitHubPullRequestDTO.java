package com.gemini.dorametricsviewer.infrastructure.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

record GitHubPullRequestDTO(
    String id,
    String number,
    @JsonProperty("merge_commit_sha") String mergeCommitSha,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("merged_at") Instant mergedAt,
    User user,
    @JsonProperty("html_url") String htmlUrl
) {
    record User(String login) {}
}
