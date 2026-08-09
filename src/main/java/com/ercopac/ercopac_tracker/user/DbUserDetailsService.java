package com.ercopac.ercopac_tracker.user;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public DbUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // AuthController accepts email addresses case-insensitively.  The
        // authentication provider must use the same lookup rule, otherwise a
        // valid account found by the controller can still fail password
        // authentication when the caller changes the email casing.
        AppUser u = repo.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles(u.getRole().name()) 
                .disabled(!u.isActive())
                .build();
    }
}
