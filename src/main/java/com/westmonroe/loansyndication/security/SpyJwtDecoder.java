package com.westmonroe.loansyndication.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class SpyJwtDecoder implements JwtDecoder {

    private JwtDecoder delegate;
    private Logger logger = LoggerFactory.getLogger(SpyJwtDecoder.class);

    public SpyJwtDecoder(String jwks) {
        this.delegate =
         NimbusJwtDecoder.withJwkSetUri(jwks).build();
    }
    @Override
    public Jwt decode(String token) throws JwtException {
        this.logger.info("Trying to verify token: {}", token);
        return this.delegate.decode(token);
    }
}
