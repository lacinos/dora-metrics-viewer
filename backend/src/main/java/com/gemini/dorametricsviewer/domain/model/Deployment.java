package com.gemini.dorametricsviewer.domain.model;

import java.time.Instant;

public record Deployment(
    String id,
    String repositoryUrl,
    String commitSha,
    Instant createdAt,
    Instant deployedAt,
    String environment,
    String status,
    String description
) {}