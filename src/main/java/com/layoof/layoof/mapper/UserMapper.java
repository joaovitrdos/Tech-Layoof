package com.layoof.layoof.mapper;

import com.layoof.layoof.dto.response.RegisterResponseDto;
import com.layoof.layoof.dto.response.UserResponseDto;
import com.layoof.layoof.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponseDto toResponse(User user);

    RegisterResponseDto toRegisterResponse(User user);

    List<UserResponseDto> toResponseList(List<User> users);
}
