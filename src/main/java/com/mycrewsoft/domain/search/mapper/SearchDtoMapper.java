package com.mycrewsoft.domain.search.mapper;

import com.mycrewsoft.domain.search.dto.response.SearchResponse;
import com.mycrewsoft.domain.search.vo.SearchVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SearchDtoMapper {

    @Mapping(target = "type", expression = "java(com.mycrewsoft.domain.search.SearchType.valueOf(vo.getType()))")
    SearchResponse toResponse(SearchVO vo);
}