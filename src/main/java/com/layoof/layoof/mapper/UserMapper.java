package com.layoof.layoof.mapper;

import com.layoof.layoof.dto.response.PublicUserResponseDto;
import com.layoof.layoof.dto.response.RegisterResponseDto;
import com.layoof.layoof.dto.response.SearchUserResponseDto;
import com.layoof.layoof.dto.response.UserResponseDto;
import com.layoof.layoof.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "badges", source = "confidence.badges")
    UserResponseDto toResponse(User user);

    RegisterResponseDto toRegisterResponse(User user);

    @Mapping(target = "badges", source = "confidence.badges")
    PublicUserResponseDto toPublicResponse(User user);

    List<UserResponseDto> toResponseList(List<User> users);

    @Mapping(target = "badges", source = "confidence.badges")
    SearchUserResponseDto toSearchResponse(User user);

    List<SearchUserResponseDto> toSearchResponseList(List<User> users);
}
