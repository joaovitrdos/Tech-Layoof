package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.SearchUserRequestDto;
import com.layoof.layoof.dto.request.UpdateUserRequestDto;
import com.layoof.layoof.dto.response.PublicUserResponseDto;
import com.layoof.layoof.dto.response.SearchUserResponseDto;
import com.layoof.layoof.dto.response.UserResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.exception.UserNotFoundException;
import com.layoof.layoof.mapper.UserMapper;
import com.layoof.layoof.repository.LayoofRepository;
import com.layoof.layoof.repository.UserRepository;
import com.layoof.layoof.uploadFile.FileUpload;
import com.layoof.layoof.uploadFile.ImageUploader;
import com.layoof.layoof.util.EmailNormalizer;
import com.layoof.layoof.util.LayoofNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String AVATAR_FOLDER = "avatars/";

    private final UserRepository userRepository;
    private final LayoofRepository layoofRepository;
    private final UserMapper userMapper;
    private final ImageUploader imageUploader;

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

    @Transactional(readOnly = true)
    public List<SearchUserResponseDto> searchUser(String name, UUID loggedUserId) {
        return userMapper.toSearchResponseList(
                userRepository.findByNameContainingIgnoreCaseAndUserIdNot(name.trim(), loggedUserId));
    }

    @Transactional
    public UserResponseDto updateProfile(UUID userId, UpdateUserRequestDto request) {
        User user = findEntityById(userId);
        user.setName(request.name().trim());
        user.setPicture(LayoofNormalizer.canonicalUrl(request.picture()));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto updatePicture(UUID userId, FileUpload file) {
        User user = findEntityById(userId);
        String picture = imageUploader.upload(file, AVATAR_FOLDER + user.getUserId());

        imageUploader.deleteByUrl(user.getPicture());
        user.setPicture(picture);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto deletePicture(UUID userId) {
        User user = findEntityById(userId);

        imageUploader.deleteByUrl(user.getPicture());
        user.setPicture(null);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID userId) {
        User user = findEntityById(userId);
        imageUploader.deleteByUrl(user.getPicture());
        layoofRepository.detachAuthor(user);
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
