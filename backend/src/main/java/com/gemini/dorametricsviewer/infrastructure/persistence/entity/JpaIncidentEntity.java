package com.gemini.dorametricsviewer.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JpaIncidentEntity {
    @Id
    private String id;
    private String repositoryUrl;
    private Instant createdAt;
    private Instant resolvedAt;
    private String severity;
    private String description;
}
