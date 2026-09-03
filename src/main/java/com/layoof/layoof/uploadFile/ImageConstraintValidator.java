package com.layoof.layoof.uploadFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@RequiredArgsConstructor
public class ImageConstraintValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private static final String UNREADABLE = "Nao foi possivel ler o arquivo enviado";

    private final ImageValidator imageValidator;

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return reject(context, imageValidator.check(null, 0).orElse(UNREADABLE));
        }

        Optional<String> problem;
        try {
            problem = imageValidator.check(signatureOf(file), file.getSize());
        } catch (IOException ex) {
            return reject(context, UNREADABLE);
        }

        return problem.map(message -> reject(context, message)).orElse(true);
    }

    private byte[] signatureOf(MultipartFile file) throws IOException {
        try (InputStream content = file.getInputStream()) {
            return content.readNBytes(ImageValidator.SIGNATURE_LENGTH);
        }
    }

    private boolean reject(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
