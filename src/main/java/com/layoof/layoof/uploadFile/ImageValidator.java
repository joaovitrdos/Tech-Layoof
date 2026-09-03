package com.layoof.layoof.uploadFile;

import com.layoof.layoof.enums.ImageType;
import com.layoof.layoof.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.Optional;

@Component
public class ImageValidator {

    public static final int SIGNATURE_LENGTH = 16;

    private static final long BYTES_IN_MEGABYTE = 1024L * 1024L;

    private final long maxBytes;

    public ImageValidator(@Value("${layoof.storage.max-image-size:5MB}") DataSize maxImageSize) {
        this.maxBytes = maxImageSize.toBytes();
    }

    public Optional<String> check(byte[] signature, long size) {
        if (signature == null || size == 0) {
            return Optional.of("Nenhum arquivo foi enviado");
        }
        if (size > maxBytes) {
            return Optional.of("A imagem deve ter no maximo " + (maxBytes / BYTES_IN_MEGABYTE) + "MB");
        }
        if (ImageType.detect(signature).isEmpty()) {
            return Optional.of("Formato nao suportado. Envie uma imagem " + ImageType.accepted());
        }
        return Optional.empty();
    }

    public ImageType validate(FileUpload file) {
        byte[] content = file == null ? null : file.content();
        long size = file == null ? 0 : file.size();

        Optional<String> problem = check(content, size);
        if (problem.isPresent()) {
            throw new InvalidFileException(problem.get());
        }

        return ImageType.detect(content).orElseThrow(
                () -> new InvalidFileException("Formato nao suportado. Envie uma imagem " + ImageType.accepted()));
    }
}
