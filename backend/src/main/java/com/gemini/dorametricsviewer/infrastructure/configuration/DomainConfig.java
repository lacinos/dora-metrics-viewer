package com.gemini.dorametricsviewer.infrastructure.configuration;

import com.gemini.dorametricsviewer.domain.LeadTimeCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public LeadTimeCalculator leadTimeCalculator() {
        return new LeadTimeCalculator();
    }
}
