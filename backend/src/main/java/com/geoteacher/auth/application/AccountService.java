package com.geoteacher.auth.application;

import com.geoteacher.auth.domain.UserAccount;
import com.geoteacher.auth.infrastructure.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class AccountService {

    private static final int MIN_PASSWORD_LENGTH = 12;

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AccountService(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            Clock clock
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public Optional<UserAccount> createInitialAccount(BootstrapAdminProperties properties) {
        if (repository.count() > 0 || properties.password() == null || properties.password().isBlank()) {
            return Optional.empty();
        }

        validateNewPassword(properties.password());
        var account = UserAccount.create(
                properties.username(),
                properties.displayName(),
                passwordEncoder.encode(properties.password()),
                Instant.now(clock)
        );
        return Optional.of(repository.save(account));
    }

    @Transactional
    public void recordSuccessfulLogin(String username) {
        var account = findRequired(username);
        account.recordSuccessfulLogin(Instant.now(clock));
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        validateNewPassword(newPassword);
        var account = findRequired(username);
        if (!passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw new AccountException("CURRENT_PASSWORD_INVALID", "Mật khẩu hiện tại không chính xác.");
        }
        if (passwordEncoder.matches(newPassword, account.getPasswordHash())) {
            throw new AccountException("PASSWORD_UNCHANGED", "Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        account.changePassword(passwordEncoder.encode(newPassword), Instant.now(clock));
    }

    @Transactional(readOnly = true)
    public UserAccount findRequired(String username) {
        return repository.findByUsernameNormalized(UserAccount.normalizeUsername(username))
                .orElseThrow(() -> new AccountException("ACCOUNT_NOT_FOUND", "Không tìm thấy tài khoản."));
    }

    private void validateNewPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > 128) {
            throw new AccountException(
                    "PASSWORD_POLICY_VIOLATION",
                    "Mật khẩu phải có từ 12 đến 128 ký tự."
            );
        }
    }
}
