package com.gemini.dorametricsviewer.domain.model;

import java.time.Instant;

public record Change(
    String id,
    String repositoryUrl,
    String commitSha,
    Instant createdAt,
    Instant mergedAt,
    String author
) {}
