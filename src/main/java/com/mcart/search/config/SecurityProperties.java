package com.mcart.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private boolean enabled = false;

    private List<String> corsAllowedOrigins = new ArrayList<>();

    private String requiredScope;
}
