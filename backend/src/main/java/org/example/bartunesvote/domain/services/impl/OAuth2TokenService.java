package org.example.bartunesvote.domain.services.impl;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service
public class OAuth2TokenService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GlobalTokenService globalTokenService;

    public OAuth2TokenService(OAuth2AuthorizedClientService authorizedClientService,
                              GlobalTokenService globalTokenService) {
        this.authorizedClientService = authorizedClientService;
        this.globalTokenService = globalTokenService;
    }

    public void storeAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );
        if (client != null && client.getAccessToken() != null) {
            globalTokenService.storeAccessToken(client.getAccessToken().getTokenValue());
        }
    }

    public String getAccessToken() {
        return globalTokenService.getAccessToken();
    }

    public boolean isTokenAvailable() {
        return globalTokenService.isTokenAvailable();
    }
}

