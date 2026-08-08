package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.UpdateUserRequestDto;
import com.layoof.layoof.dto.response.UserResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.exception.UserNotFoundException;
import com.layoof.layoof.mapper.UserMapper;
import com.layoof.layoof.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDto findById(UUID userId) {
        return userMapper.toResponse(findEntityById(userId));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findByEmail(String email) {
        String normalized = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new UserNotFoundException("Nenhum usuario encontrado com o email: " + email));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponseDto updateProfile(UUID userId, UpdateUserRequestDto request) {
        User user = findEntityById(userId);
        user.setName(request.name().trim());
        user.setPicture(request.picture());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID userId) {
        userRepository.delete(findEntityById(userId));
    }

    public long count() {
        return userRepository.count();
    }

    /**
     * Uso interno: os metodos publicos expoem DTO, mas as operacoes de escrita
     * precisam da entidade gerenciada pelo JPA.
     */
    private User findEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Nenhum usuario encontrado com o id: " + userId));
    }
}
