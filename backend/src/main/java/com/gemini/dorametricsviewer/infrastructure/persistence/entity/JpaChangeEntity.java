package com.gemini.dorametricsviewer.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "changes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JpaChangeEntity {
    @Id
    private String id;
    private String repositoryUrl;
    private String commitSha;
    private Instant createdAt;
    private Instant mergedAt;
    private String author;
}
