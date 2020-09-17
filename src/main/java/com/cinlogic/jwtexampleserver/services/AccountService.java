package com.cinlogic.jwtexampleserver.services;

import com.cinlogic.jwtexampleserver.data.models.Account;
import com.cinlogic.jwtexampleserver.services.dtos.CreateAccountRequest;
import com.cinlogic.jwtexampleserver.services.dtos.UpdateAccountRequest;

import java.util.UUID;

public interface AccountService {

    Account getByAccountId(UUID accountId);
    Account getByUsername(String username);
    Account getByUsernameAndPassword(String username, String password);

    Account createAccount(CreateAccountRequest request);
    Account updateAccount(UUID accountId, UpdateAccountRequest request);
    void changePassword(UUID accountId, String currentPassword, String newPassword);

}
