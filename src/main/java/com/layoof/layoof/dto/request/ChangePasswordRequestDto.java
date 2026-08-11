package com.layoof.layoof.dto.request;

public record ChangePasswordRequestDto(
        String currentPassword,
        String newPassword
) {
}
