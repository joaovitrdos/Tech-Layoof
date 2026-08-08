package com.layoof.layoof.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.layoof.layoof.dto.request.GoogleAuthRequestDto;
import com.layoof.layoof.dto.request.LoginRequestDto;
import com.layoof.layoof.dto.request.RegisterRequestDto;
import com.layoof.layoof.dto.response.AuthResponseDto;
import com.layoof.layoof.dto.response.RegisterResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.AuthProvider;
import com.layoof.layoof.exception.EmailAlreadyInUseException;
import com.layoof.layoof.exception.GoogleAccountAlreadyExistsException;
import com.layoof.layoof.exception.InvalidCredentialsException;
import com.layoof.layoof.exception.InvalidGoogleTokenException;
import com.layoof.layoof.exception.InvalidRegistrationDataException;
import com.layoof.layoof.infra.security.TokenService;
import com.layoof.layoof.mapper.UserMapper;
import com.layoof.layoof.mapper.UserMapperImpl;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private TokenService tokenService;

    /* Mapper e factory sao puros: usar as implementacoes reais mantem as asserts nos DTOs. */
    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @Spy
    private EmailFactory emailFactory = new EmailFactory();

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("cria usuario LOCAL com a senha codificada e o email normalizado")
        void deveCriarUsuarioLocal() {
            when(userRepository.existsByEmail("joao@el.com.br")).thenReturn(false);
            when(passwordEncoder.encode("segredo123")).thenReturn("hash-bcrypt");
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RegisterResponseDto response = authService.register(
                    new RegisterRequestDto("  Joao Oliveira  ", "  JOAO@EL.COM.BR  ", "segredo123"));

            assertThat(response.name()).isEqualTo("Joao Oliveira");
            assertThat(response.email()).isEqualTo("joao@el.com.br");
            assertThat(response.authProvider()).isEqualTo(AuthProvider.LOCAL);

            /* A senha nao aparece no DTO (de proposito): confere na entidade persistida. */
            verify(userRepository).saveAndFlush(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("hash-bcrypt");
        }

        @Test
        @DisplayName("traduz a violacao da constraint unica em conflito de email")
        void deveTratarCorridaNoEmailUnico() {
            when(userRepository.existsByEmail("joao@el.com.br")).thenReturn(false);
            when(passwordEncoder.encode("segredo123")).thenReturn("hash-bcrypt");
            when(userRepository.saveAndFlush(any(User.class)))
                    .thenThrow(new DataIntegrityViolationException("uk_users_email"));

            assertThatThrownBy(() -> authService.register(
                    new RegisterRequestDto("Joao", "joao@el.com.br", "segredo123")))
                    .isInstanceOf(EmailAlreadyInUseException.class)
                    .hasMessageContaining("joao@el.com.br");

            verify(emailSenderService, never()).sendAsync(any(EmailMessage.class), any(User.class));
        }

        @Test
        @DisplayName("recusa o cadastro sem senha antes de tocar o banco")
        void deveRecusarSemSenha() {
            assertThatThrownBy(() -> authService.register(
                    new RegisterRequestDto("Joao", "joao@el.com.br", "   ")))
                    .isInstanceOf(InvalidRegistrationDataException.class)
                    .hasMessage(InvalidRegistrationDataException.MISSING_PASSWORD);

            verify(userRepository, never()).saveAndFlush(any(User.class));
        }

        @Test
        @DisplayName("dispara o email de boas-vindas para o usuario salvo")
        void deveEnviarEmailDeBoasVindas() {
            when(userRepository.existsByEmail("joao@el.com.br")).thenReturn(false);
            when(passwordEncoder.encode("segredo123")).thenReturn("hash-bcrypt");
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            authService.register(new RegisterRequestDto("Joao Oliveira", "joao@el.com.br", "segredo123"));

            ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
            verify(emailSenderService).sendAsync(emailCaptor.capture(), any(User.class));

            EmailMessage sent = emailCaptor.getValue();
            assertThat(sent.getRecipient()).isEqualTo("joao@el.com.br");
            assertThat(sent.getBody()).contains("Joao Oliveira");
        }

        @Test
        @DisplayName("falha quando o email ja esta cadastrado")
        void deveFalharComEmailDuplicado() {
            when(userRepository.existsByEmail("joao@el.com.br")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(
                    new RegisterRequestDto("Joao", "joao@el.com.br", "segredo123")))
                    .isInstanceOf(EmailAlreadyInUseException.class)
                    .hasMessageContaining("joao@el.com.br");

            verify(userRepository, never()).saveAndFlush(any(User.class));
            verify(emailSenderService, never()).sendAsync(any(EmailMessage.class), any(User.class));
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("retorna o token e os dados do usuario quando a senha confere")
        void deveAutenticar() {
            User stored = User.builder()
                    .name("Joao")
                    .email("joao@el.com.br")
                    .password("hash-bcrypt")
                    .authProvider(AuthProvider.LOCAL)
                    .build();

            when(userRepository.findByEmail("joao@el.com.br")).thenReturn(Optional.of(stored));
            when(passwordEncoder.matches("segredo123", "hash-bcrypt")).thenReturn(true);
            when(tokenService.generateToken(stored)).thenReturn("jwt-token");

            AuthResponseDto response = authService.login(new LoginRequestDto("JOAO@EL.COM.BR", "segredo123"));

            assertThat(response.accessToken()).isEqualTo("jwt-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.user().email()).isEqualTo("joao@el.com.br");
            assertThat(response.user().name()).isEqualTo("Joao");
        }

        @Test
        @DisplayName("falha quando o usuario nao existe")
        void deveFalharComUsuarioInexistente() {
            when(userRepository.findByEmail("joao@el.com.br")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(new LoginRequestDto("joao@el.com.br", "segredo123")))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("falha quando a senha nao confere")
        void deveFalharComSenhaIncorreta() {
            User stored = User.builder()
                    .email("joao@el.com.br")
                    .password("hash-bcrypt")
                    .authProvider(AuthProvider.LOCAL)
                    .build();

            when(userRepository.findByEmail("joao@el.com.br")).thenReturn(Optional.of(stored));
            when(passwordEncoder.matches("errada", "hash-bcrypt")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(new LoginRequestDto("joao@el.com.br", "errada")))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("orienta a usar o Google quando a conta nao tem senha local")
        void deveFalharComContaDoGoogle() {
            User stored = User.builder()
                    .email("joao@gmail.com")
                    .googleId("google-123")
                    .authProvider(AuthProvider.GOOGLE)
                    .build();

            when(userRepository.findByEmail("joao@gmail.com")).thenReturn(Optional.of(stored));

            assertThatThrownBy(() -> authService.login(new LoginRequestDto("joao@gmail.com", "segredo123")))
                    .isInstanceOf(GoogleAccountAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("loginWithGoogle")
    class LoginWithGoogle {

        @Test
        @DisplayName("cria um usuario novo a partir do payload do Google")
        void deveCriarUsuarioDoGoogle() {
            when(googleTokenVerifier.verify("id-token")).thenReturn(
                    payload("google-123", "JOAO@GMAIL.COM", "Joao Oliveira", "https://foto.google/joao.png"));
            when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("joao@gmail.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuthResponseDto response = authService.loginWithGoogle(new GoogleAuthRequestDto("id-token"));

            assertThat(response.user().email()).isEqualTo("joao@gmail.com");
            assertThat(response.user().name()).isEqualTo("Joao Oliveira");
            assertThat(response.user().picture()).isEqualTo("https://foto.google/joao.png");
            assertThat(response.user().authProvider()).isEqualTo(AuthProvider.GOOGLE);

            /* googleId nao faz parte do contrato de resposta: confere na entidade. */
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getGoogleId()).isEqualTo("google-123");
        }

        @Test
        @DisplayName("vincula o googleId a uma conta local ja existente sem trocar o provider")
        void deveVincularContaExistente() {
            User stored = User.builder()
                    .name("Joao")
                    .email("joao@gmail.com")
                    .password("hash-bcrypt")
                    .authProvider(AuthProvider.LOCAL)
                    .build();

            when(googleTokenVerifier.verify("id-token")).thenReturn(
                    payload("google-123", "joao@gmail.com", "Joao", "https://foto.google/joao.png"));
            when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("joao@gmail.com")).thenReturn(Optional.of(stored));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuthResponseDto response = authService.loginWithGoogle(new GoogleAuthRequestDto("id-token"));

            assertThat(response.user().picture()).isEqualTo("https://foto.google/joao.png");
            assertThat(response.user().authProvider()).isEqualTo(AuthProvider.LOCAL);

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getGoogleId()).isEqualTo("google-123");
        }

        @Test
        @DisplayName("usa o email como nome quando o payload nao traz a claim name")
        void deveUsarEmailComoNome() {
            when(googleTokenVerifier.verify("id-token")).thenReturn(
                    payload("google-123", "joao@gmail.com", null, null));
            when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("joao@gmail.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuthResponseDto response = authService.loginWithGoogle(new GoogleAuthRequestDto("id-token"));

            assertThat(response.user().name()).isEqualTo("joao@gmail.com");
            assertThat(response.user().picture()).isNull();
        }

        @Test
        @DisplayName("falha quando o payload nao traz o subject")
        void deveFalharSemSubject() {
            when(googleTokenVerifier.verify("id-token")).thenReturn(payload(null, "joao@gmail.com", "Joao", null));

            assertThatThrownBy(() -> authService.loginWithGoogle(new GoogleAuthRequestDto("id-token")))
                    .isInstanceOf(InvalidGoogleTokenException.class)
                    .hasMessageContaining("identificador");
        }

        @Test
        @DisplayName("falha quando o payload nao traz o email")
        void deveFalharSemEmail() {
            when(googleTokenVerifier.verify("id-token")).thenReturn(payload("google-123", null, "Joao", null));

            assertThatThrownBy(() -> authService.loginWithGoogle(new GoogleAuthRequestDto("id-token")))
                    .isInstanceOf(InvalidGoogleTokenException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        @DisplayName("recusa o token quando o Google nao verificou o e-mail")
        void deveFalharComEmailNaoVerificado() {
            GoogleIdToken.Payload payload = payload("google-123", "joao@empresa.com", "Joao", null);
            payload.setEmailVerified(false);

            when(googleTokenVerifier.verify("id-token")).thenReturn(payload);

            assertThatThrownBy(() -> authService.loginWithGoogle(new GoogleAuthRequestDto("id-token")))
                    .isInstanceOf(InvalidGoogleTokenException.class)
                    .hasMessage(InvalidGoogleTokenException.EMAIL_NOT_VERIFIED);

            verify(userRepository, never()).save(any(User.class));
        }

        private GoogleIdToken.Payload payload(String subject, String email, String name, String picture) {
            GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
            payload.setSubject(subject);
            payload.setEmail(email);
            payload.setEmailVerified(true);
            if (name != null) {
                payload.set("name", name);
            }
            if (picture != null) {
                payload.set("picture", picture);
            }
            return payload;
        }
    }
}
