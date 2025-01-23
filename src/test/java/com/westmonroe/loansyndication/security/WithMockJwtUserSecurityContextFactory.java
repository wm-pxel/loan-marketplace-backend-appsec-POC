package com.westmonroe.loansyndication.security;

import com.westmonroe.loansyndication.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class WithMockJwtUserSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtUser> {

    private UserService userService;

    public WithMockJwtUserSecurityContextFactory(UserService userService) {
        this.userService = userService;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser user) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claims(claims -> {
                    claims.put("sub", user.username());
                    claims.put("email", user.username());
                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                })
                .build();
        UserDetails principal = userService.loadUserByUsername(user.username());
        Authentication auth = new JwtAuthenticationToken(jwt, principal.getAuthorities());
        context.setAuthentication(auth);

        return context;
    }

}