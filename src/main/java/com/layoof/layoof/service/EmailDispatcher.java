package com.layoof.layoof.service;

import com.layoof.layoof.notification.EmailRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EmailDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EmailDispatcher.class);

    private final EmailSenderService emailSenderService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailRequested(EmailRequestedEvent event) {
        try {
            emailSenderService.send(event.message(), event.user());
        } catch (RuntimeException ex) {
            log.error("Falha ao enviar o email '{}' para {}",
                    event.message().getSubject(), event.message().getRecipient(), ex);
        }
    }
}
