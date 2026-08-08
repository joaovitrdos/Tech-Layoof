package com.layoof.layoof.notification;

import com.layoof.layoof.enums.TypeEmail;

public interface EmailMessage {
    String getRecipient();
    String getSubject();
    String getBody();
    TypeEmail getType();
}
