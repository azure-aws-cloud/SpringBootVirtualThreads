package com.example.security.springbootvirtualthreads.config;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerConfigCustomizer externalApiCBConfig() {
        return CircuitBreakerConfigCustomizer.of("externalApiCB",
                builder -> builder
                        .slidingWindowSize(4)
                        .failureRateThreshold(50) // 2 failures out of 4 trips breaker
                        .permittedNumberOfCallsInHalfOpenState(1)
                        .waitDurationInOpenState(Duration.ofSeconds(5))


        );
    }
}

