package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/** Login por senha numa conta que so tem vinculo com o Google. */
public class GoogleAccountAlreadyExistsException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String TITLE = "Conta vinculada ao Google";

    public static final String USE_GOOGLE_BUTTON =
            "Esta conta foi criada com o Google. Entre usando o botao 'Entrar com Google'";

    public GoogleAccountAlreadyExistsException() {
        this(USE_GOOGLE_BUTTON);
    }

    public GoogleAccountAlreadyExistsException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public GoogleAccountAlreadyExistsException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
