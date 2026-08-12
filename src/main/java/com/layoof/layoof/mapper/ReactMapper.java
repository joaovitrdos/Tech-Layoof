package com.layoof.layoof.mapper;

import com.layoof.layoof.dto.response.ReactResponseDto;
import com.layoof.layoof.entity.React;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReactMapper {

    @Mapping(target = "commentId", source = "comment.commentId")
    @Mapping(target = "authorId", source = "author.userId")
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "authorPicture", source = "author.picture")
    ReactResponseDto toResponse(React react);

    List<ReactResponseDto> toResponseList(List<React> reacts);
}
