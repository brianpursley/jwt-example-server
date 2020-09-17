package com.cinlogic.jwtexampleserver.security;

import com.cinlogic.jwtexampleserver.data.models.Account;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.UUID;

public class ApplicationUser extends User {

    private UUID accountId;
    private String email;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ApplicationUser(Account account) {
        super(account.getUsername(), account.getPassword(), Collections.emptyList());
        setAccountId(account.getAccountId());
        setEmail(account.getEmail());
    }
}
