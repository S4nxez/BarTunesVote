package org.example.bartunesvote.ui.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bartunesvote.domain.services.impl.OAuth2TokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class OAuth2TokenFilter extends OncePerRequestFilter {

    private final OAuth2TokenService tokenService;

    public OAuth2TokenFilter(OAuth2TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // Obtener el token desde tu servicio
        String token = tokenService.getAccessToken();

        if (token != null) {
            // Añadir el token como encabezado en la solicitud
            response.setHeader("Authorization", "Bearer " + token);
        }

        // Incluir encabezado personalizado ngrok-skip-browser-warning
        response.setHeader("ngrok-skip-browser-warning", "1231");

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
