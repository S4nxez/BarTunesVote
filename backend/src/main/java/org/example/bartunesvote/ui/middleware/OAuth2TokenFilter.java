package org.example.bartunesvote.ui.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bartunesvote.domain.services.impl.OAuth2TokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.NonNull;

import java.io.IOException;

@Component
public class OAuth2TokenFilter extends OncePerRequestFilter {

    private final OAuth2TokenService tokenService;

    public OAuth2TokenFilter(OAuth2TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException, ServletException {
        String token = tokenService.getAccessToken();
        if (token != null) response.setHeader("Authorization", "Bearer " + token);
        response.setHeader("ngrok-skip-browser-warning", "1231");
        filterChain.doFilter(request, response);
    }
}
