package com.layoof.layoof.notification;

import com.layoof.layoof.entity.User;

public record EmailRequestedEvent(EmailMessage message, User user) {
}
