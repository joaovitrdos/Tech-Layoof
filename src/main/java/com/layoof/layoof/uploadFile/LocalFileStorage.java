package com.layoof.layoof.uploadFile;

import com.layoof.layoof.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@ConditionalOnProperty(name = "layoof.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStore {

    private final Path root;
    private final String baseUrl;

    public LocalFileStorage(@Value("${layoof.storage.local-directory:uploads}") Path localDirectory,
                            @Value("${layoof.storage.public-url:http://localhost:8080/files}") String publicUrl) {
        this.root = localDirectory.toAbsolutePath().normalize();
        this.baseUrl = FileStore.normalizeBaseUrl(publicUrl);
    }

    @Override
    public StoredFile upload(FileUpload file, String key) {
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, file.content());
        } catch (IOException ex) {
            throw new FileStorageException("Nao foi possivel salvar a imagem em disco", ex);
        }
        return new StoredFile(key, publicUrl(key));
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ex) {
            throw new FileStorageException("Nao foi possivel remover a imagem do disco", ex);
        }
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

    private Path resolve(String key) {
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new FileStorageException("Caminho de arquivo invalido: " + key);
        }
        return target;
    }
}
