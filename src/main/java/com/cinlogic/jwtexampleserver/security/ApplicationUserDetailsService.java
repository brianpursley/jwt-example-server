package com.cinlogic.jwtexampleserver.security;

import com.cinlogic.jwtexampleserver.services.AccountService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final AccountService accountService;

    public ApplicationUserDetailsService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public ApplicationUser loadUserByUsername(String username) throws UsernameNotFoundException {
        if (StringUtils.isEmpty(username)) {
            throw new UsernameNotFoundException("Username not found");
        }

        var account = accountService.getByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        return new ApplicationUser(account);
    }
}
