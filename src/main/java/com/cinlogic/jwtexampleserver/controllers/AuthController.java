package com.cinlogic.jwtexampleserver.controllers;

import com.cinlogic.jwtexampleserver.controllers.dtos.GetTokenRequest;
import com.cinlogic.jwtexampleserver.data.models.Account;
import com.cinlogic.jwtexampleserver.services.AccountService;
import com.cinlogic.jwtexampleserver.services.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";

    private final TokenService tokenService;
    private final AccountService accountService;

    AuthController(TokenService tokenService, AccountService accountService) {
        this.tokenService = tokenService;
        this.accountService = accountService;
    }

    @PostMapping(path="/token")
    public ResponseEntity<String> getToken(@RequestBody final GetTokenRequest body, HttpServletRequest request, HttpServletResponse response)
    {
        LOG.debug("getToken for {}", body.getUsername());

        final Account account = accountService.getByUsernameAndPassword(body.getUsername(), body.getPassword());
        if (account == null) {
            LOG.warn("account not found for username {}", body.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        LOG.debug("account id {} found for username {}", account.getAccountId(), body.getUsername());
        return makeTokenResponse(account, request, response);
    }

    @PostMapping(path="/token/refresh")
    public ResponseEntity<String> refreshToken(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME) String refreshToken, HttpServletRequest request, HttpServletResponse response)
    {
        try {
            LOG.debug("refreshToken for {}", refreshToken);

            final UUID accountId = tokenService.getAccountIdFromRefreshToken(refreshToken);
            LOG.debug("refreshToken accountId = {}", accountId);

            final Account account = accountService.getByAccountId(accountId);
            if (account == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return makeTokenResponse(account, request, response);
        } catch (ExpiredJwtException e) {
            LOG.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private ResponseEntity<String> makeTokenResponse(Account account, HttpServletRequest request, HttpServletResponse response) {
        var cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, tokenService.generateRefreshToken(account));
        cookie.setPath("/auth/token");
        cookie.setHttpOnly(true);
        if (request.isSecure()) {
            cookie.setSecure(true);
        }
        response.addCookie(cookie);

        return ResponseEntity.ok(tokenService.generateAccessToken(account));
    }
}