package com.gemini.dorametricsviewer;

import com.gemini.dorametricsviewer.domain.port.SourceControlPort;
import com.gemini.dorametricsviewer.infrastructure.github.mock.FakeGitHubAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfig {

    @Bean(name = "gitHubAdapter") // Overwrite the real bean by name
    @Primary
    public SourceControlPort gitHubAdapter() {
        System.out.println("!!! REGISTERING FAKE GITHUB ADAPTER (Overwriting gitHubAdapter) !!!");
        return new FakeGitHubAdapter();
    }
}
