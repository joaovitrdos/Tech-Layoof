package com.layoof.layoof.enums;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public enum ImageType {

    JPEG("image/jpeg", ".jpg"),
    PNG("image/png", ".png"),
    WEBP("image/webp", ".webp"),
    GIF("image/gif", ".gif");

    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] GIF_SIGNATURE = "GIF8".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] RIFF_SIGNATURE = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP_SIGNATURE = "WEBP".getBytes(StandardCharsets.US_ASCII);

    private static final int WEBP_OFFSET = 8;

    private final String contentType;
    private final String extension;

    ImageType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }

    public static Optional<ImageType> detect(byte[] content) {
        if (content == null) {
            return Optional.empty();
        }
        if (matches(content, JPEG_SIGNATURE, 0)) {
            return Optional.of(JPEG);
        }
        if (matches(content, PNG_SIGNATURE, 0)) {
            return Optional.of(PNG);
        }
        if (matches(content, GIF_SIGNATURE, 0)) {
            return Optional.of(GIF);
        }
        if (matches(content, RIFF_SIGNATURE, 0) && matches(content, WEBP_SIGNATURE, WEBP_OFFSET)) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    public static String accepted() {
        return "JPEG, PNG, WEBP ou GIF";
    }

    private static boolean matches(byte[] content, byte[] signature, int offset) {
        if (content.length < offset + signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if (content[offset + index] != signature[index]) {
                return false;
            }
        }
        return true;
    }
}
