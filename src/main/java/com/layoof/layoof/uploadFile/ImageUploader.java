package com.layoof.layoof.uploadFile;

import com.layoof.layoof.enums.ImageType;
import com.layoof.layoof.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ImageUploader {

    private static final Logger log = LoggerFactory.getLogger(ImageUploader.class);

    private final FileStore fileStore;
    private final ImageValidator imageValidator;

    public String upload(FileUpload file, String folder) {
        ImageType type = imageValidator.validate(file);
        String key = folder + "/" + UUID.randomUUID() + type.extension();

        return fileStore.upload(new FileUpload(file.fileName(), type.contentType(), file.content()), key).url();
    }

    public void deleteByUrl(String url) {
        fileStore.keyFrom(url).ifPresent(this::deleteQuietly);
    }

    private void deleteQuietly(String key) {
        try {
            fileStore.delete(key);
        } catch (FileStorageException ex) {
            log.warn("Nao foi possivel remover o arquivo {}: {}", key, ex.getMessage());
        }
    }
}
