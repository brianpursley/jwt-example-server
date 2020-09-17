package com.cinlogic.jwtexampleserver.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="jwt")
public class JwtConfigurationProperties {
    private String issuer;
    private String audience;
    private JwtTokenProperties accessToken;
    private JwtTokenProperties refreshToken;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public JwtTokenProperties getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(JwtTokenProperties accessToken) {
        this.accessToken = accessToken;
    }

    public JwtTokenProperties getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(JwtTokenProperties refreshToken) {
        this.refreshToken = refreshToken;
    }

    public static class JwtTokenProperties {
        private int duration;
        private String secretKey;

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}
