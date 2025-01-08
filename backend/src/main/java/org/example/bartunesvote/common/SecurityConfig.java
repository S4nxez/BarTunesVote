package org.example.bartunesvote.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors().and()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/oauth2/**", "/api/vote").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/songs/46BC7zm67B71WZu19pYo9Q", false) // Redirigir tras login exitoso
                        .failureUrl("/login?error=true")      // Redirigir tras login fallido
                ).exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    // Si el usuario no está autenticado, no redirigirlo al login, sino que devuelvas un error
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No authenticated user");
                });
        return http.build();
    }
}
