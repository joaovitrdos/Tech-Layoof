package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.UpdateUserRequestDto;
import com.layoof.layoof.dto.response.PublicUserResponseDto;
import com.layoof.layoof.dto.response.SearchUserResponseDto;
import com.layoof.layoof.dto.response.UserResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.exception.InvalidURLLinkedinException;
import com.layoof.layoof.exception.ProfileNotOwnedException;
import com.layoof.layoof.exception.UserNotFoundException;
import com.layoof.layoof.mapper.UserMapper;
import com.layoof.layoof.repository.LayoofRepository;
import com.layoof.layoof.repository.UserRepository;
import com.layoof.layoof.uploadFile.FileUpload;
import com.layoof.layoof.uploadFile.FileUploads;
import com.layoof.layoof.uploadFile.ImageSourceRule;
import com.layoof.layoof.uploadFile.ImageUploader;
import com.layoof.layoof.util.EmailNormalizer;
import com.layoof.layoof.util.LayoofNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
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

        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        List<User> found = userRepository.searchByName(name.trim()).stream()
                .filter(user -> !user.getUserId().equals(loggedUserId))
                .toList();

        return userMapper.toSearchResponseList(found);
    }

    @Transactional
    public UserResponseDto updateProfile(UUID userId, UpdateUserRequestDto request, MultipartFile file) {
        User user = findEntityById(userId);
        ImageSourceRule.requireAtMostOne(file, request.picture());

        String previousPicture = user.getPicture();
        String picture = ImageSourceRule.hasFile(file)
                ? imageUploader.upload(FileUploads.from(file), avatarFolder(user))
                : LayoofNormalizer.canonicalUrl(request.picture());
        String linkedinURL = requireAvailableLinkedin(request.linkedinURL(), userId);

        user.setName(request.name().trim());
        user.setPicture(picture);
        user.setLinkedinURL(linkedinURL);

        if (!Objects.equals(previousPicture, picture)) {
            imageUploader.deleteByUrl(previousPicture);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    private String requireAvailableLinkedin(String value, UUID userId) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String linkedinURL = LayoofNormalizer.canonicalUrl(value);

        if (linkedinURL == null) {
            throw new InvalidURLLinkedinException("O endereco do LinkedIn nao e uma url valida");
        }

        userRepository.findByLinkedinURL(linkedinURL)
                .filter(owner -> !owner.getUserId().equals(userId))
                .ifPresent(owner -> {
                    throw new InvalidURLLinkedinException("Esta conta do LinkedIn ja esta vinculada a outro usuario");
                });

        return linkedinURL;
    }

    @Transactional
    public UserResponseDto updatePicture(UUID userId, User loggedUser, FileUpload file) {
        User user = requireSelf(findEntityById(userId), loggedUser,
                "Voce so pode trocar a imagem do seu proprio perfil");

        String picture = imageUploader.upload(file, avatarFolder(user));

        imageUploader.deleteByUrl(user.getPicture());
        user.setPicture(picture);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID userId) {
        User user = findEntityById(userId);
        imageUploader.deleteByUrl(user.getPicture());
        layoofRepository.detachAuthor(user);
        userRepository.delete(user);
    }

    private User requireSelf(User user, User loggedUser, String message) {
        if (loggedUser == null || !user.getUserId().equals(loggedUser.getUserId())) {
            throw new ProfileNotOwnedException(message);
        }
        return user;
    }

    private String avatarFolder(User user) {
        return AVATAR_FOLDER + user.getUserId();
    }

    private User findEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Nenhum usuario encontrado com o id: " + userId));
    }

    public long count() {
        return userRepository.count();
    }
}
