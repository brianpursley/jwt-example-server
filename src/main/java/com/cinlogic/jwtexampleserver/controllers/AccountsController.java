package com.cinlogic.jwtexampleserver.controllers;

import com.cinlogic.jwtexampleserver.controllers.dtos.ChangePasswordRequest;
import com.cinlogic.jwtexampleserver.data.models.Account;
import com.cinlogic.jwtexampleserver.security.ApplicationUser;
import com.cinlogic.jwtexampleserver.services.AccountService;
import com.cinlogic.jwtexampleserver.services.dtos.CreateAccountRequest;
import com.cinlogic.jwtexampleserver.services.dtos.UpdateAccountRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/accounts")
public class AccountsController {

    private final AccountService accountService;

    AccountsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest request) throws DataIntegrityViolationException {
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @GetMapping(path = "/current")
    public ResponseEntity<Account> getCurrentAccount(@AuthenticationPrincipal ApplicationUser principal) {
        return ResponseEntity.ok(accountService.getByAccountId(principal.getAccountId()));
    }

    @PutMapping(path = "/current")
    public ResponseEntity<Account> updateAccount(@AuthenticationPrincipal ApplicationUser principal,
                                                 @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(principal.getAccountId(), request));
    }

    @PutMapping(path = "/current/password")
    public ResponseEntity changePassword(@AuthenticationPrincipal ApplicationUser principal,
                                         @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(principal.getAccountId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

}