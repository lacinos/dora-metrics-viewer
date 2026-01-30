package com.gemini.dorametricsviewer.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "deployments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JpaDeploymentEntity {
    @Id
    private String id;
    private String repositoryUrl;
    private String commitSha;
    private Instant createdAt;
    private Instant deployedAt;
    private String environment;
    private String status;
    
    @Lob
    private String description;
}