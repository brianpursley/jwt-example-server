package com.cinlogic.jwtexampleserver.security;

import com.cinlogic.jwtexampleserver.services.AccountService;
import com.cinlogic.jwtexampleserver.services.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final AccountService accountService;
    private final TokenService tokenService;

    JwtAuthenticationProvider(AccountService accountService, TokenService tokenService) {
        this.accountService = accountService;
        this.tokenService = tokenService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        //
    }

    @Override
    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

        Object token = usernamePasswordAuthenticationToken.getCredentials();
        if (token == null) {
            throw new BadCredentialsException("token is null");
        }

        UUID accountId;
        try {
            accountId = tokenService.getAccountIdFromAccessToken(String.valueOf(token));
            if (accountId == null) {
                throw new BadCredentialsException("Account ID is null");
            }
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("Expired token", e);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid token", e);
        }

        var account = accountService.getByAccountId(accountId);
        if (account == null) {
            throw new BadCredentialsException("Account not found");
        }

        return new ApplicationUser(account);
    }
}
