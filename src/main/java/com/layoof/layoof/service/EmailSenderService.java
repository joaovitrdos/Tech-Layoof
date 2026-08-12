package com.layoof.layoof.service;

import com.layoof.layoof.entity.Email;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.StatusEmail;
import com.layoof.layoof.exception.EmailSendException;
import com.layoof.layoof.notification.EmailMessage;
import com.layoof.layoof.repository.EmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;
    private final String from;
    private final boolean enabled;

    public EmailSenderService(JavaMailSender mailSender,
                              EmailRepository emailRepository,
                              @Value("${layoof.mail.from:${spring.mail.username:}}") String from,
                              @Value("${layoof.mail.enabled:true}") boolean enabled) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
        this.from = from == null ? "" : from.trim();
        this.enabled = enabled;
    }

    public void send(EmailMessage emailMessage, User user) {
        Objects.requireNonNull(emailMessage, "emailMessage nao pode ser nulo");
        Objects.requireNonNull(user, "user nao pode ser nulo");

        String recipient = emailMessage.getRecipient();
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Destinatario do email nao pode ser vazio");
        }

        if (!enabled) {
            log.info("Envio de email desabilitado (layoof.mail.enabled=false). Assunto '{}' para {} ignorado",
                    emailMessage.getSubject(), recipient);
            return;
        }

        Email emailEntity = new Email();
        emailEntity.setUserId(user.getUserId());
        emailEntity.setEmailFrom(this.from);
        emailEntity.setEmailTo(recipient.trim());
        emailEntity.setSubject(emailMessage.getSubject());
        emailEntity.setTypeEmail(emailMessage.getType());

        try {
            mailSender.send(buildMessage(recipient, emailMessage));

        } catch (MailException ex) {
            emailEntity.setStatusEmail(StatusEmail.ERROR);
            registrar(emailEntity);
            log.error("Nao foi possivel enviar o email '{}' para {}", emailMessage.getSubject(), recipient, ex);
            throw new EmailSendException("Falha ao enviar o email para: " + recipient, ex);
        }

        emailEntity.setStatusEmail(StatusEmail.SENT);
        registrar(emailEntity);
        log.info("Email '{}' enviado para {}", emailMessage.getSubject(), recipient);
    }

    private void registrar(Email emailEntity) {
        try {
            emailRepository.save(emailEntity);
        } catch (DataAccessException ex) {
            log.error("Email para {} terminou como {}, mas a auditoria nao foi gravada",
                    emailEntity.getEmailTo(), emailEntity.getStatusEmail(), ex);
        }
    }

    private SimpleMailMessage buildMessage(String recipient, EmailMessage emailMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (!from.isEmpty()) {
            message.setFrom(from);
        }
        message.setTo(recipient.trim());
        message.setSubject(emailMessage.getSubject());
        message.setText(emailMessage.getBody());
        return message;
    }
}