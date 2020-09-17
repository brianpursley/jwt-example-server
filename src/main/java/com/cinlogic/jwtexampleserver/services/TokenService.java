package com.cinlogic.jwtexampleserver.services;

import com.cinlogic.jwtexampleserver.data.models.Account;

import java.util.UUID;

public interface TokenService {
    String generateAccessToken(Account account);
    String generateRefreshToken(Account account);
    UUID getAccountIdFromAccessToken(String token);
    UUID getAccountIdFromRefreshToken(String token);
}
