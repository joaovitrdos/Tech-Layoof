package com.layoof.layoof.service;

import com.layoof.layoof.entity.User;
import com.layoof.layoof.entity.VerificationCode;
import com.layoof.layoof.enums.AuthProvider;
import com.layoof.layoof.exception.InvalidVerificationCodeException;
import com.layoof.layoof.repository.VerificationCodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationCodeService")
class VerificationCodeServiceTest {

    private static final String EMAIL = "joao@el.com.br";

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @InjectMocks
    private VerificationCodeService verificationCodeService;

    @Captor
    private ArgumentCaptor<VerificationCode> codeCaptor;

    @Nested
    @DisplayName("createCode")
    class CreateCode {

        @Test
        @DisplayName("gera um codigo de 6 digitos valido por 15 minutos e ainda nao usado")
        void deveGerarCodigoDeSeisDigitos() {
            when(verificationCodeRepository.findAllByUserEmailAndUsedFalse(EMAIL)).thenReturn(List.of());
            when(verificationCodeRepository.save(any(VerificationCode.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LocalDateTime antes = LocalDateTime.now();
            String code = verificationCodeService.createCode(user());

            assertThat(code).matches("\\d{6}");

            verify(verificationCodeRepository).save(codeCaptor.capture());
            VerificationCode saved = codeCaptor.getValue();
            assertThat(saved.getCode()).isEqualTo(code);
            assertThat(saved.getUsed()).isFalse();
            assertThat(saved.getExpiresAt()).isBetween(
                    antes.plus(VerificationCodeService.CODE_TTL).minusSeconds(5),
                    LocalDateTime.now().plus(VerificationCodeService.CODE_TTL).plusSeconds(5));
        }

        @Test
        @DisplayName("derruba os codigos pendentes para que so o ultimo enviado valha")
        void deveInvalidarCodigosAnteriores() {
            VerificationCode pendente = VerificationCode.builder().code("111111").build();

            when(verificationCodeRepository.findAllByUserEmailAndUsedFalse(EMAIL))
                    .thenReturn(List.of(pendente));
            when(verificationCodeRepository.save(any(VerificationCode.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            verificationCodeService.createCode(user());

            assertThat(pendente.getUsed()).isTrue();
            verify(verificationCodeRepository).saveAll(List.of(pendente));
        }

        @Test
        @DisplayName("normaliza o email do usuario ao procurar os codigos pendentes")
        void deveNormalizarEmailDoUsuario() {
            User user = user();
            user.setEmail("  JOAO@EL.COM.BR  ");

            when(verificationCodeRepository.findAllByUserEmailAndUsedFalse(EMAIL)).thenReturn(List.of());
            when(verificationCodeRepository.save(any(VerificationCode.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            verificationCodeService.createCode(user);

            verify(verificationCodeRepository).findAllByUserEmailAndUsedFalse(EMAIL);
        }
    }

    @Nested
    @DisplayName("validateCode")
    class ValidateCode {

        @Test
        @DisplayName("aceita o codigo vigente sem consumi-lo")
        void deveAceitarSemConsumir() {
            VerificationCode vigente = codigoValido();
            when(verificationCodeRepository.findByCodeAndUserEmailAndUsedFalse("123456", EMAIL))
                    .thenReturn(Optional.of(vigente));

            verificationCodeService.validateCode("123456", "  JOAO@EL.COM.BR  ");

            assertThat(vigente.getUsed()).isFalse();
            verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
        }

        @Test
        @DisplayName("recusa um codigo inexistente ou ja usado")
        void deveRecusarCodigoInexistente() {
            when(verificationCodeRepository.findByCodeAndUserEmailAndUsedFalse("123456", EMAIL))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> verificationCodeService.validateCode("123456", EMAIL))
                    .isInstanceOf(InvalidVerificationCodeException.class)
                    .hasMessage(InvalidVerificationCodeException.INVALID_CODE);
        }

        @Test
        @DisplayName("recusa um codigo expirado")
        void deveRecusarCodigoExpirado() {
            VerificationCode expirado = codigoValido();
            expirado.setExpiresAt(LocalDateTime.now().minusMinutes(1));

            when(verificationCodeRepository.findByCodeAndUserEmailAndUsedFalse("123456", EMAIL))
                    .thenReturn(Optional.of(expirado));

            assertThatThrownBy(() -> verificationCodeService.validateCode("123456", EMAIL))
                    .isInstanceOf(InvalidVerificationCodeException.class)
                    .hasMessage(InvalidVerificationCodeException.EXPIRED_CODE);

            verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
        }
    }

    @Nested
    @DisplayName("consumeCode")
    class ConsumeCode {

        @Test
        @DisplayName("marca o codigo como usado para impedir um segundo uso")
        void deveMarcarComoUsado() {
            VerificationCode vigente = codigoValido();
            when(verificationCodeRepository.findByCodeAndUserEmailAndUsedFalse("123456", EMAIL))
                    .thenReturn(Optional.of(vigente));

            verificationCodeService.consumeCode("123456", EMAIL);

            verify(verificationCodeRepository).save(codeCaptor.capture());
            assertThat(codeCaptor.getValue().getUsed()).isTrue();
        }

        @Test
        @DisplayName("nao consome nada quando o codigo esta expirado")
        void naoDeveConsumirCodigoExpirado() {
            VerificationCode expirado = codigoValido();
            expirado.setExpiresAt(LocalDateTime.now().minusMinutes(1));

            when(verificationCodeRepository.findByCodeAndUserEmailAndUsedFalse("123456", EMAIL))
                    .thenReturn(Optional.of(expirado));

            assertThatThrownBy(() -> verificationCodeService.consumeCode("123456", EMAIL))
                    .isInstanceOf(InvalidVerificationCodeException.class);

            assertThat(expirado.getUsed()).isFalse();
            verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
        }
    }

    private static User user() {
        return User.builder()
                .name("Joao")
                .email(EMAIL)
                .password("hash-bcrypt")
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    private static VerificationCode codigoValido() {
        return VerificationCode.builder()
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .user(user())
                .build();
    }
}
