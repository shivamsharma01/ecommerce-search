package com.mcart.search.config;

import com.mcart.search.security.JsonAccessDeniedHandler;
import com.mcart.search.security.JsonAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * OAuth2 resource server (JWT) when {@code app.security.enabled=true}, matching user/product service patterns.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<JwtDecoder> jwtDecoderProvider) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);

        if (!CollectionUtils.isEmpty(securityProperties.getCorsAllowedOrigins())) {
            http.cors(c -> c.configurationSource(corsConfigurationSource(securityProperties.getCorsAllowedOrigins())));
        }

        if (securityProperties.isEnabled()) {
            if (jwtDecoderProvider.getIfAvailable() == null) {
                throw new IllegalStateException(
                        "app.security.enabled=true requires spring.security.oauth2.resourceserver.jwt.issuer-uri "
                                + "or spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
            }
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
            http.authorizeHttpRequests(auth -> {
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                auth.requestMatchers("/health", "/health/**").permitAll();
                auth.requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll();
                auth.requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll();
                auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/webjars/swagger-ui/**").permitAll();

                String scope = securityProperties.getRequiredScope();
                if (StringUtils.hasText(scope)) {
                    String authority = "SCOPE_" + scope.trim();
                    auth.requestMatchers(HttpMethod.GET, "/api/**").authenticated();
                    auth.requestMatchers(HttpMethod.HEAD, "/api/**").authenticated();
                    auth.requestMatchers(HttpMethod.POST, "/api/**").hasAuthority(authority);
                    auth.requestMatchers(HttpMethod.PUT, "/api/**").hasAuthority(authority);
                    auth.requestMatchers(HttpMethod.DELETE, "/api/**").hasAuthority(authority);
                    auth.requestMatchers(HttpMethod.PATCH, "/api/**").hasAuthority(authority);
                } else {
                    auth.requestMatchers("/api/**").authenticated();
                }
                auth.anyRequest().denyAll();
            });
            http.exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }

    private static CorsConfigurationSource corsConfigurationSource(List<String> origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
