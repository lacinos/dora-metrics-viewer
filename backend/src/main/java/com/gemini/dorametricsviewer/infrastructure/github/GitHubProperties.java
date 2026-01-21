package com.gemini.dorametricsviewer.infrastructure.github;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "dora.github")
@Data
public class GitHubProperties {
    private String token;
    private String baseUrl = "https://api.github.com";
}
