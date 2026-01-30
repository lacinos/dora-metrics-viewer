package com.gemini.dorametricsviewer.infrastructure.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

record GitHubReleaseDTO(
    String id,
    @JsonProperty("tag_name") String tagName,
    @JsonProperty("target_commitish") String targetCommitish,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("published_at") Instant publishedAt,
    @JsonProperty("html_url") String htmlUrl,
    String body
) {}