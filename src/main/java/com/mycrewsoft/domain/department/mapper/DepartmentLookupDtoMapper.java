package com.mycrewsoft.domain.department.mapper;

import com.mycrewsoft.domain.department.dto.response.DepartmentLookupResponse;
import com.mycrewsoft.domain.department.vo.DepartmentLookupVO;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentLookupDtoMapper {

    DepartmentLookupResponse toResponse(DepartmentLookupVO vo);

    List<DepartmentLookupResponse> toResponseList(List<DepartmentLookupVO> voList);
}