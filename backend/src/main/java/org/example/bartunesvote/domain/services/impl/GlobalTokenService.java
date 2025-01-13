package org.example.bartunesvote.domain.services.impl;

import org.springframework.stereotype.Service;

@Service
public class GlobalTokenService {

    private String accessToken;

    public synchronized void storeAccessToken(String token) {
        this.accessToken = token;
    }

    public synchronized String getAccessToken() {
        return this.accessToken;
    }

    public synchronized boolean isTokenAvailable() {
        return this.accessToken != null;
    }
}

