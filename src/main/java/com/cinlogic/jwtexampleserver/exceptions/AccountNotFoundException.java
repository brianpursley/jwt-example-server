package com.cinlogic.jwtexampleserver.exceptions;

import java.util.UUID;

public class AccountNotFoundException extends ValidationException {
    public AccountNotFoundException(UUID accountId) {
        this("accountId", accountId);
    }

    public AccountNotFoundException(String identifierName, Object identifierValue) {
        super(String.format("Account not found with %s %s", identifierName, identifierValue));
    }
}
