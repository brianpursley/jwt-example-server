package com.cinlogic.jwtexampleserver.services;

import com.cinlogic.jwtexampleserver.data.models.Account;
import com.cinlogic.jwtexampleserver.data.repositories.AccountRepository;
import com.cinlogic.jwtexampleserver.exceptions.AccountNotFoundException;
import com.cinlogic.jwtexampleserver.exceptions.ConflictException;
import com.cinlogic.jwtexampleserver.exceptions.ValidationException;
import com.cinlogic.jwtexampleserver.services.dtos.CreateAccountRequest;
import com.cinlogic.jwtexampleserver.services.dtos.UpdateAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

    private static final List<String> blacklistedUsernames = List.of();

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    AccountServiceImpl(
            AccountRepository accountRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public Account getByAccountId(UUID accountId) {
        final var result = accountRepository.findById(accountId);
        return result.isEmpty() ? null : result.get();
    }

    @Override
    public Account getByUsername(String username) {
        final var result = accountRepository.findByUsername(username);
        return result.isEmpty() ? null : result.get();
    }

    @Override
    public Account getByUsernameAndPassword(String username, String password) {
        final var account = getByUsername(username);
        if (account == null) {
            return null;
        }
        return bCryptPasswordEncoder.matches(password, account.getPassword()) ? account : null;
    }

    @Override
    public Account createAccount(CreateAccountRequest request) {
        validateUsername(null, request);
        validateEmail(null, request);
        validatePassword(request);

        var account = new Account();
        account.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account = accountRepository.save(account);

        LOG.info("account created for username {}", request.getUsername());
        return account;
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Account updateAccount(UUID accountId, UpdateAccountRequest request) {
        validateUsername(accountId, request);
        validateEmail(accountId, request);

        var account = getByAccountId(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }

        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account = accountRepository.save(account);

        LOG.info("account updated for id {}", accountId);
        return account;
    }

    @Override
    public void changePassword(UUID accountId, String currentPassword, String newPassword) {
        validatePassword(newPassword);

        var account = getByAccountId(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }

        if (!bCryptPasswordEncoder.matches(currentPassword, account.getPassword())) {
            throw new ValidationException("Current password does not match");
        }

        account.setPassword(bCryptPasswordEncoder.encode(newPassword));
        accountRepository.save(account);

        LOG.info("Password changed for {}", account.getUsername());
    }

    private void validatePassword(CreateAccountRequest request) {
        validatePassword(request.getPassword());
    }

    private void validatePassword(String password) {
        if (isBlank(password)) {
            throw new ValidationException("Password is required");
        }
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
    }

    private void validateUsername(UUID accountId, UpdateAccountRequest request) {
        if (isBlank(request.getUsername())) {
            throw new ValidationException("Username is required");
        }
        if (blacklistedUsernames.contains(request.getUsername())) {
            throw new ConflictException("Username not available");
        }
        final var existingAccount = accountRepository.findByUsername(request.getUsername());
        if (existingAccount.isPresent()) {
            if (accountId == null || !existingAccount.get().getAccountId().equals(accountId)) {
                throw new ConflictException("Username not available");
            }
        }
    }

    private void validateEmail(UUID accountId, UpdateAccountRequest request) {
        if (isBlank(request.getEmail())) {
            throw new ValidationException("Email is required");
        }
        final var existingAccount = accountRepository.findByEmail(request.getEmail());
        if (existingAccount.isPresent()) {
            if (accountId == null || !existingAccount.get().getAccountId().equals(accountId)) {
                throw new ConflictException("Email is already in use by another account");
            }
        }
    }

}
