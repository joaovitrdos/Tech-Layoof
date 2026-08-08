package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.ResetPasswordRequestDto;
import com.layoof.layoof.dto.request.SendEmailRequestDto;
import com.layoof.layoof.dto.request.ValidateCodeRequestDto;
import com.layoof.layoof.dto.response.ResetPasswordResponseDto;
import com.layoof.layoof.dto.response.SendEmailResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.AuthProvider;
import com.layoof.layoof.enums.TypeEmail;
import com.layoof.layoof.exception.InvalidVerificationCodeException;
import com.layoof.layoof.notification.EmailFactory;
import com.layoof.layoof.notification.EmailMessage;
import com.layoof.layoof.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordRecoveryService")
class PasswordRecoveryServiceTest {

    private static final String EMAIL = "joao@el.com.br";
    private static final String CODE = "123456";

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private PasswordEncoder passwordEncoder;

    /* Factory pura: a implementacao real mantem as asserts no email gerado. */
    @Spy
    private EmailFactory emailFactory = new EmailFactory();

    @InjectMocks
    private PasswordRecoveryService passwordRecoveryService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Nested
    @DisplayName("sendRecoveryCode")
    class SendRecoveryCode {

        @Test
        @DisplayName("envia o codigo por email para a conta local e normaliza o endereco")
        void deveEnviarCodigo() {
            User stored = localUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(stored));
            when(verificationCodeService.createCode(stored)).thenReturn(CODE);

            SendEmailResponseDto response = passwordRecoveryService.sendRecoveryCode(
                    new SendEmailRequestDto("  JOAO@EL.COM.BR  "));

            assertThat(response.mensagem()).isEqualTo(PasswordRecoveryService.CODE_SENT_MESSAGE);

            ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
            verify(emailSenderService).sendAsync(emailCaptor.capture(), any(User.class));

            EmailMessage sent = emailCaptor.getValue();
            assertThat(sent.getRecipient()).isEqualTo(EMAIL);
            assertThat(sent.getType()).isEqualTo(TypeEmail.REFRESH_PASSWORD);
            assertThat(sent.getBody()).contains(CODE);
        }

        @Test
        @DisplayName("responde a mesma mensagem quando o email nao existe, sem gerar codigo")
        void naoDeveRevelarEmailInexistente() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            SendEmailResponseDto response = passwordRecoveryService.sendRecoveryCode(
                    new SendEmailRequestDto(EMAIL));

            assertThat(response.mensagem()).isEqualTo(PasswordRecoveryService.CODE_SENT_MESSAGE);
            verify(verificationCodeService, never()).createCode(any(User.class));
            verify(emailSenderService, never()).sendAsync(any(EmailMessage.class), any(User.class));
        }

        @Test
        @DisplayName("nao envia codigo para conta do Google, que nao tem senha local")
        void naoDeveEnviarParaContaDoGoogle() {
            User google = User.builder()
                    .name("Joao")
                    .email(EMAIL)
                    .googleId("google-123")
                    .authProvider(AuthProvider.GOOGLE)
                    .build();

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(google));

            SendEmailResponseDto response = passwordRecoveryService.sendRecoveryCode(
                    new SendEmailRequestDto(EMAIL));

            assertThat(response.mensagem()).isEqualTo(PasswordRecoveryService.CODE_SENT_MESSAGE);
            verify(verificationCodeService, never()).createCode(any(User.class));
            verify(emailSenderService, never()).sendAsync(any(EmailMessage.class), any(User.class));
        }
    }

    @Nested
    @DisplayName("validateCode")
    class ValidateCode {

        @Test
        @DisplayName("delega a conferencia do codigo sem consumi-lo")
        void deveDelegarValidacao() {
            passwordRecoveryService.validateCode(new ValidateCodeRequestDto(CODE, EMAIL));

            verify(verificationCodeService).validateCode(CODE, EMAIL);
            verify(verificationCodeService, never()).consumeCode(anyString(), anyString());
        }

        @Test
        @DisplayName("propaga a falha quando o codigo nao vale mais")
        void devePropagarCodigoInvalido() {
            doThrow(new InvalidVerificationCodeException(InvalidVerificationCodeException.EXPIRED_CODE))
                    .when(verificationCodeService).validateCode(CODE, EMAIL);

            assertThatThrownBy(() -> passwordRecoveryService.validateCode(new ValidateCodeRequestDto(CODE, EMAIL)))
                    .isInstanceOf(InvalidVerificationCodeException.class)
                    .hasMessage(InvalidVerificationCodeException.EXPIRED_CODE);
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("consome o codigo e grava a nova senha codificada")
        void deveTrocarASenha() {
            User stored = localUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(stored));
            when(passwordEncoder.encode("nova-senha123")).thenReturn("novo-hash");

            ResetPasswordResponseDto response = passwordRecoveryService.resetPassword(
                    new ResetPasswordRequestDto(CODE, "  JOAO@EL.COM.BR  ", "nova-senha123"));

            assertThat(response.message()).isEqualTo(PasswordRecoveryService.PASSWORD_UPDATED_MESSAGE);

            verify(verificationCodeService).consumeCode(CODE, EMAIL);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("novo-hash");
        }

        @Test
        @DisplayName("nao grava a senha quando o codigo nao vale mais")
        void naoDeveTrocarComCodigoInvalido() {
            User stored = localUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(stored));
            doThrow(new InvalidVerificationCodeException(InvalidVerificationCodeException.INVALID_CODE))
                    .when(verificationCodeService).consumeCode(CODE, EMAIL);

            assertThatThrownBy(() -> passwordRecoveryService.resetPassword(
                    new ResetPasswordRequestDto(CODE, EMAIL, "nova-senha123")))
                    .isInstanceOf(InvalidVerificationCodeException.class)
                    .hasMessage(InvalidVerificationCodeException.INVALID_CODE);

            assertThat(stored.getPassword()).isEqualTo("hash-bcrypt");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("trata email inexistente como codigo invalido, sem revelar o cadastro")
        void naoDeveRevelarEmailInexistente() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordRecoveryService.resetPassword(
                    new ResetPasswordRequestDto(CODE, EMAIL, "nova-senha123")))
                    .isInstanceOf(InvalidVerificationCodeException.class)
                    .hasMessage(InvalidVerificationCodeException.INVALID_CODE);

            verify(verificationCodeService, never()).consumeCode(anyString(), anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    private static User localUser() {
        return User.builder()
                .name("Joao")
                .email(EMAIL)
                .password("hash-bcrypt")
                .authProvider(AuthProvider.LOCAL)
                .build();
    }
}
