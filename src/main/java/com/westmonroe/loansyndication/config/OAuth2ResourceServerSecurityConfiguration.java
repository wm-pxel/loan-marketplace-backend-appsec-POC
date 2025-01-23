package com.westmonroe.loansyndication.config;

import com.westmonroe.loansyndication.security.JwtAuthEntryPointAccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = false, jsr250Enabled = false)
public class OAuth2ResourceServerSecurityConfiguration {

    private final JwtAuthEntryPointAccessHandler jwtAuthEntryPointAccessHandler;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    String jwkSetUri;

    public OAuth2ResourceServerSecurityConfiguration(JwtAuthEntryPointAccessHandler jwtAuthEntryPointAccessHandler) {
        this.jwtAuthEntryPointAccessHandler = jwtAuthEntryPointAccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf(crsf -> crsf.disable())
            .authorizeHttpRequests(requests -> requests
                    .requestMatchers(antMatcher("/graphiql")).permitAll()
                    .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> {
                exception.authenticationEntryPoint(jwtAuthEntryPointAccessHandler);
                exception.accessDeniedHandler(jwtAuthEntryPointAccessHandler);
            })
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));

        // @formatter:on
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }

}