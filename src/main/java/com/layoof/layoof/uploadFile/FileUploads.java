package com.layoof.layoof.uploadFile;

import com.layoof.layoof.exception.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public final class FileUploads {

    private FileUploads() {
    }

    public static FileUpload from(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Nenhum arquivo foi enviado");
        }
        try {
            return new FileUpload(file.getOriginalFilename(), file.getContentType(), file.getBytes());
        } catch (IOException ex) {
            throw new InvalidFileException("Nao foi possivel ler o arquivo enviado", ex);
        }
    }
}
