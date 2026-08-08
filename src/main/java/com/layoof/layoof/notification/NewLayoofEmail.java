package com.layoof.layoof.notification;

import com.layoof.layoof.enums.TypeEmail;

public class NewLayoofEmail implements EmailMessage{
    private final String email;
    private final String layoffDetails;

    public NewLayoofEmail(String email, String layoffDetails) {
        this.email = email;
        this.layoffDetails = layoffDetails;
    }

    @Override
    public String getRecipient() {
        return email;
    }

    @Override
    public String getSubject() {
        return "Mais um layoof meu cara";
    }

    @Override
    public String getBody() {
        return this.layoffDetails;
    }

    @Override
    public TypeEmail getType() {
        return TypeEmail.NEW_LAYOOF;
    }
}
