package com.layoof.layoof.mapper;

import com.layoof.layoof.dto.response.CommentReponseDto;
import com.layoof.layoof.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(target = "layoofId", source = "layoof.id")
    @Mapping(target = "authorId", source = "author.userId")
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "authorPicture", source = "author.picture")
    CommentReponseDto toResponse(Comment comment);

    List<CommentReponseDto> toResponseList(List<Comment> comments);
}
