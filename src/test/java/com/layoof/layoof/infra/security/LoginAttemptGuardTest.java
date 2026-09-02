package com.layoof.layoof.infra.security;

import com.layoof.layoof.exception.TooManyAttemptsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LoginAttemptGuard")
class LoginAttemptGuardTest {

    private static final String EMAIL = "joao@el.com.br";

    private final LoginAttemptGuard guard = new LoginAttemptGuard();

    @Test
    @DisplayName("deixa passar quem ainda nao errou")
    void shouldAllowAccountWithoutFailures() {
        assertThatCode(() -> guard.assertNotLocked(EMAIL)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ainda deixa tentar antes de esgotar as tentativas")
    void shouldAllowBelowLimit() {
        for (int tentativa = 0; tentativa < LoginAttemptGuard.MAX_FAILURES - 1; tentativa++) {
            guard.recordFailure(EMAIL);
        }

        assertThatCode(() -> guard.assertNotLocked(EMAIL)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("tranca a conta depois das tentativas erradas seguidas")
    void shouldLockAtLimit() {
        for (int tentativa = 0; tentativa < LoginAttemptGuard.MAX_FAILURES; tentativa++) {
            guard.recordFailure(EMAIL);
        }

        assertThatThrownBy(() -> guard.assertNotLocked(EMAIL))
                .isInstanceOf(TooManyAttemptsException.class)
                .hasMessageContaining("Muitas tentativas");
    }

    @Test
    @DisplayName("tranca so a conta tentada e nao as outras")
    void shouldLockOnlyTheAttemptedAccount() {
        for (int tentativa = 0; tentativa < LoginAttemptGuard.MAX_FAILURES; tentativa++) {
            guard.recordFailure(EMAIL);
        }

        assertThatCode(() -> guard.assertNotLocked("outro@el.com.br")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("zera o contador quando o login enfim da certo")
    void shouldResetAfterSuccess() {
        for (int tentativa = 0; tentativa < LoginAttemptGuard.MAX_FAILURES - 1; tentativa++) {
            guard.recordFailure(EMAIL);
        }
        guard.recordSuccess(EMAIL);
        guard.recordFailure(EMAIL);

        assertThatCode(() -> guard.assertNotLocked(EMAIL)).doesNotThrowAnyException();
    }
}
