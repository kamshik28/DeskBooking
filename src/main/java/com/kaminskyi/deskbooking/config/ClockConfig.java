package com.kaminskyi.deskbooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    // separate bean so tests can use a fixed date
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
