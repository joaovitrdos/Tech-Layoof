package com.layoof.layoof.uploadFile;

public record FileUpload(String fileName, String contentType, byte[] content) {

    public int size() {
        return content == null ? 0 : content.length;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
