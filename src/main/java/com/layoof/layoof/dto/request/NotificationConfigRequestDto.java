package com.layoof.layoof.dto.request;

import com.layoof.layoof.enums.NotificationFrequency;
import jakarta.validation.constraints.NotNull;

public record NotificationConfigRequestDto(

        @NotNull(message = "A frequencia de notificacao e obrigatoria")
        NotificationFrequency frequency
) {
}
