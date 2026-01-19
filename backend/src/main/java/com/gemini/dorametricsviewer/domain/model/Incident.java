package com.gemini.dorametricsviewer.domain.model;

import java.time.Instant;

public record Incident(
    String id,
    String repositoryUrl,
    Instant createdAt,
    Instant resolvedAt,
    String severity,
    String description
) {}
