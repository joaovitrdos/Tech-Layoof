package com.layoof.layoof.uploadFile;

import java.util.Optional;

public interface FileStore {

    StoredFile upload(FileUpload file, String key);

    void delete(String key);

    String baseUrl();

    default String publicUrl(String key) {
        return baseUrl() + "/" + key;
    }

    default Optional<String> keyFrom(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        String prefix = baseUrl() + "/";
        return url.startsWith(prefix)
                ? Optional.of(url.substring(prefix.length()))
                : Optional.empty();
    }

    static String normalizeBaseUrl(String url) {
        return url == null || url.isBlank() ? "" : url.trim().replaceAll("/+$", "");
    }
}
