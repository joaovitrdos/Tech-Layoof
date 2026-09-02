package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.UpdateUserRequestDto;
import com.layoof.layoof.dto.response.PublicUserResponseDto;
import com.layoof.layoof.dto.response.UserResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.exception.UserNotFoundException;
import com.layoof.layoof.mapper.UserMapper;
import com.layoof.layoof.repository.LayoofRepository;
import com.layoof.layoof.repository.SessionRepository;
import com.layoof.layoof.repository.UserRepository;
import com.layoof.layoof.util.EmailNormalizer;
import com.layoof.layoof.util.LayoofNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LayoofRepository layoofRepository;
    private final SessionRepository sessionRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDto findById(UUID userId) {
        return userMapper.toResponse(findEntityById(userId));
    }

    @Transactional(readOnly = true)
    public PublicUserResponseDto findPublicById(UUID userId) {
        return userMapper.toPublicResponse(findEntityById(userId));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findByEmail(String email) {
        User user = userRepository.findByEmail(EmailNormalizer.normalize(email))
                .orElseThrow(() -> new UserNotFoundException("Nenhum usuario encontrado com o email informado"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponseDto updateProfile(UUID userId, UpdateUserRequestDto request) {
        User user = findEntityById(userId);
        user.setName(request.name().trim());
        user.setPicture(LayoofNormalizer.canonicalUrl(request.picture()));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID userId) {
        User user = findEntityById(userId);
        layoofRepository.detachAuthor(user);
        sessionRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }

    private User findEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Nenhum usuario encontrado com o id: " + userId));
    }

    public long count() {
        return userRepository.count();
    }
}
