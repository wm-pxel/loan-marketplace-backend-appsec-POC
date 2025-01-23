package com.westmonroe.loansyndication.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.AuthorizationException;
import com.westmonroe.loansyndication.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.westmonroe.loansyndication.utils.SecurityUtils.writeErrorResponse;

@Component
@Slf4j
public class UserAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserAuthenticationFilter(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        // Here we are for sure, context object holds a valid and decoded JWT token.
        SecurityContext context = SecurityContextHolder.getContext();

        try {

            if ( context.getAuthentication() instanceof JwtAuthenticationToken ) {

                /*
                 *  Reaching this section of code means that Cognito performed necessary authentication.  We now need
                 *  to create new token for the user or service account. The following two cases are handled (in order):
                 *     1) The token contains a custom marketplaceId claim (User Account).
                 *     2) The custom token is missing and scope is used for auth (Service Account).
                 */
                JwtAuthenticationToken auth = (JwtAuthenticationToken) context.getAuthentication();

                String userUid;

                if ( auth.getTokenAttributes().get("email") != null ) {
                    userUid = auth.getTokenAttributes().get("email").toString();
                } else {
                    String scope = auth.getTokenAttributes().get("scope").toString();
                    if (scope.contains("lamina/")) {
                        userUid = scope.replace("lamina/", "");
                    } else if (scope.contains("UserManagementAPI/")) {
                        userUid = scope.replace("UserManagementAPI/", "SystemUser-");
                    }
                    else {
                        // Token does not contain email or expected scopes
                        throw new AuthorizationException("Invalid JWT");
                    }
                }

                UserDetails principal = userService.loadUserByUsername(userUid);

                UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

            }

            chain.doFilter(request, response);

        } catch (AuthenticationException | AuthorizationException | JwtException e ) {
            writeErrorResponse(request, response, objectMapper, e.getMessage(), e.getClass().getSimpleName());
        }

    }

}