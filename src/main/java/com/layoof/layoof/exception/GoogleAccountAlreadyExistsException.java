package com.layoof.layoof.exception;

public class GoogleAccountAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "Esta conta foi criada com o Google. Entre usando o botao 'Entrar com Google'";

    public GoogleAccountAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public GoogleAccountAlreadyExistsException(String message) {
        super(message);
    }

    public GoogleAccountAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
