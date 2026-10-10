package com.kaminskyi.deskbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Booking booking, Admin admin) {

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Booking(int maxDaysAhead) {
    }

    public record Admin(String email, String password) {
    }
}
