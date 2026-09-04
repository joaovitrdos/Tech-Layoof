package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.NotificationConfigRequestDto;
import com.layoof.layoof.dto.response.NotificationConfigResponseDto;
import com.layoof.layoof.entity.NotificationConfig;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.NotificationFrequency;
import com.layoof.layoof.exception.UserNotFoundException;
import com.layoof.layoof.repository.NotificationConfigRepository;
import com.layoof.layoof.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationConfigService {

    private final NotificationConfigRepository notificationConfigRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationConfigResponseDto findByUser(UUID userId) {
        return toResponse(notificationConfigRepository.findByUserUserId(userId)
                .orElseGet(() -> notificationConfigRepository.save(newConfig(userId))));
    }

    @Transactional
    public NotificationConfigResponseDto update(UUID userId, NotificationConfigRequestDto request) {
        NotificationConfig config = notificationConfigRepository.findByUserUserId(userId)
                .orElseGet(() -> newConfig(userId));

        config.setFrequency(request.frequency());

        return toResponse(notificationConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public List<NotificationConfig> recipientsAt(LocalDateTime moment) {
        Set<NotificationFrequency> frequencies = NotificationFrequency.sendingAt(moment);

        return frequencies.isEmpty()
                ? List.of()
                : notificationConfigRepository.findByFrequencyIn(frequencies);
    }

    @Transactional
    public void registerSent(NotificationConfig config, LocalDateTime moment) {
        config.setLastSentAt(moment);
        notificationConfigRepository.save(config);
    }

    private NotificationConfig newConfig(UUID userId) {
        return NotificationConfig.builder().user(findUser(userId)).build();
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Nenhum usuario encontrado com o id"));
    }

    private NotificationConfigResponseDto toResponse(NotificationConfig config) {
        return new NotificationConfigResponseDto(
                config.getFrequency(), config.getLastSentAt(), config.getUpdatedAt());
    }
}
