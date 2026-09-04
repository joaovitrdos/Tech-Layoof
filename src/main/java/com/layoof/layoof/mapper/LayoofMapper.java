package com.layoof.layoof.mapper;

import com.layoof.layoof.dto.response.LayoofResponseDto;
import com.layoof.layoof.dto.response.SearchLayoofResponseDto;
import com.layoof.layoof.dto.response.SourceResponseDto;
import com.layoof.layoof.entity.Layoof;
import com.layoof.layoof.entity.Source;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LayoofMapper {

    @Mapping(target = "authorId", source = "author.userId")
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "authorPicture", source = "author.picture")
    LayoofResponseDto toResponse(Layoof layoof);

    SourceResponseDto toResponse(Source source);

    List<LayoofResponseDto> toResponseList(List<Layoof> layoofs);

    List<SearchLayoofResponseDto> toSearchResponseList(List<Layoof> layoofs);

}
