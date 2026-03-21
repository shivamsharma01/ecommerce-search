package com.mcart.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * JWT resource-server toggles (aligned with product service).
 */
@Data
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    /**
     * When true, {@code /api/**} requires a valid JWT; set {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}
     * or {@code jwk-set-uri}.
     */
    private boolean enabled = false;

    private List<String> corsAllowedOrigins = new ArrayList<>();

    /**
     * If set, mutating methods on {@code /api/**} require this OAuth2 scope (e.g. {@code search.write} → {@code SCOPE_search.write}).
     * GET/HEAD stay authenticated only.
     */
    private String requiredScope;
}
