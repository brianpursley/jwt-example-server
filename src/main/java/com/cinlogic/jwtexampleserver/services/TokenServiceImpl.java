package com.cinlogic.jwtexampleserver.services;

import com.cinlogic.jwtexampleserver.configuration.JwtConfigurationProperties;
import com.cinlogic.jwtexampleserver.data.models.Account;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.TextCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenServiceImpl.class);

    private static final String JWT_CLAIM_ACCOUNT_ID = "accountId";

    private final JwtConfigurationProperties jwtConfig;

    public TokenServiceImpl(JwtConfigurationProperties jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    private final SigningKeyResolver accessTokenSigningKeyResolver = new SigningKeyResolverAdapter() {
        @Override
        public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
            return TextCodec.BASE64.decode(jwtConfig.getAccessToken().getSecretKey());
        }
    };

    private final SigningKeyResolver refreshTokenSigningKeyResolver = new SigningKeyResolverAdapter() {
        @Override
        public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
            return TextCodec.BASE64.decode(jwtConfig.getRefreshToken().getSecretKey());
        }
    };

    @Override
    public String generateAccessToken(Account account) {
        return generateToken(account,
                jwtConfig.getAccessToken().getSecretKey(),
                jwtConfig.getAccessToken().getDuration());
    }

    @Override
    public String generateRefreshToken(Account account) {
        return generateToken(account,
                jwtConfig.getRefreshToken().getSecretKey(),
                jwtConfig.getRefreshToken().getDuration());
    }

    @Override
    public UUID getAccountIdFromAccessToken(String token) {
        return getAccountIdFromToken(token, accessTokenSigningKeyResolver);
    }

    @Override
    public UUID getAccountIdFromRefreshToken(String token) {
        return getAccountIdFromToken(token, refreshTokenSigningKeyResolver);
    }

    private String generateToken(Account account, String secretKey, int durationSeconds) {
        return Jwts.builder()
                .setIssuer(jwtConfig.getIssuer())
                .setAudience(jwtConfig.getAudience())
                .setSubject(account.getUsername())
                .claim(JWT_CLAIM_ACCOUNT_ID, account.getAccountId())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(durationSeconds)))
                .signWith(SignatureAlgorithm.HS512, TextCodec.BASE64.decode(secretKey))
                .compact();
    }

    private UUID getAccountIdFromToken(String token, SigningKeyResolver signingKeyResolver) {
        final Claims claims = Jwts.parser()
                .setSigningKeyResolver(signingKeyResolver)
                .parseClaimsJws(token)
                .getBody();

        if (claims.getIssuer() == null || !claims.getIssuer().equals(jwtConfig.getIssuer())) {
            LOG.warn("Issuer mismatch: {} != {}", claims.getIssuer(), jwtConfig.getIssuer());
            throw new JwtException("Issuer mismatch");
        }

        if (claims.getAudience() == null || !claims.getAudience().equals(jwtConfig.getAudience())) {
            LOG.warn("Audience mismatch: {} != {}", claims.getAudience(), jwtConfig.getAudience());
            throw new JwtException("Audience mismatch");
        }

        return UUID.fromString(claims.get(JWT_CLAIM_ACCOUNT_ID, String.class));
    }

}
