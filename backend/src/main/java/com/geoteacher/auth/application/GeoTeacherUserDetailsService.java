package com.geoteacher.auth.application;

import com.geoteacher.auth.domain.UserAccount;
import com.geoteacher.auth.domain.UserStatus;
import com.geoteacher.auth.infrastructure.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeoTeacherUserDetailsService implements UserDetailsService {

    private final UserAccountRepository repository;

    public GeoTeacherUserDetailsService(UserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var account = repository.findByUsernameNormalized(UserAccount.normalizeUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .authorities("ROLE_TEACHER")
                .disabled(account.getStatus() == UserStatus.DISABLED)
                .accountLocked(account.getStatus() == UserStatus.LOCKED)
                .build();
    }
}
