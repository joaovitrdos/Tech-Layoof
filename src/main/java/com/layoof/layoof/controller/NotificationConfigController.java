package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.NotificationConfigRequestDto;
import com.layoof.layoof.dto.response.NotificationConfigResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.service.NotificationConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationConfigController {

    private final NotificationConfigService notificationConfigService;

    @GetMapping
    public NotificationConfigResponseDto notifications(@AuthenticationPrincipal User principal) {
        return notificationConfigService.findByUser(principal.getUserId());
    }

    @PutMapping
    public NotificationConfigResponseDto update(@AuthenticationPrincipal User principal,
                                                @RequestBody @Valid NotificationConfigRequestDto request) {

        return notificationConfigService.update(principal.getUserId(), request);
    }
}
