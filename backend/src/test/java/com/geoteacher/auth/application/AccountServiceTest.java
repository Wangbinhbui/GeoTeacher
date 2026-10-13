package com.geoteacher.auth.application;

import com.geoteacher.auth.domain.UserAccount;
import com.geoteacher.auth.infrastructure.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-13T00:00:00Z");

    @Mock
    private UserAccountRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(repository, passwordEncoder, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsInitialAccountOnlyWhenDatabaseIsEmptyAndPasswordIsProvided() {
        var properties = new BootstrapAdminProperties("Teacher", "a-secure-password", "Giáo viên");
        when(repository.count()).thenReturn(0L);
        when(passwordEncoder.encode(properties.password())).thenReturn("encoded-password");
        when(repository.save(org.mockito.ArgumentMatchers.any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createInitialAccount(properties);

        assertThat(result).isPresent();
        var captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("Teacher");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void skipsBootstrapWhenPasswordIsBlank() {
        var result = service.createInitialAccount(
                new BootstrapAdminProperties("teacher", "", "Giáo viên")
        );

        assertThat(result).isEmpty();
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        var account = UserAccount.create("teacher", "Giáo viên", "old-hash", NOW);
        when(repository.findByUsernameNormalized("teacher")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(
                "teacher",
                "wrong-password",
                "a-new-secure-password"
        ))
                .isInstanceOf(AccountException.class)
                .hasMessage("Mật khẩu hiện tại không chính xác.");
    }
}
