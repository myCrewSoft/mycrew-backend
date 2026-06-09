package com.mycrewsoft.domain.employee.mapper;

import com.mycrewsoft.domain.employee.dto.response.EmployeeLookupResponse;
import com.mycrewsoft.domain.employee.vo.EmployeeLookupVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeLookupDtoMapper {

    @Mapping(source = "empId",   target = "id")
    @Mapping(source = "empNm",   target = "name")
    @Mapping(source = "deptNm",  target = "department")
    // 직책명(jobPstnNm)이 있으면 position에 직책명을 넣고, 없으면 직급명(jobGrdNm)을 넣는다.
    @Mapping(target = "position", expression = "java(vo.getJobPstnNm() != null ? vo.getJobPstnNm() : vo.getJobGrdNm())")
    @Mapping(source = "profileImageUrl", target = "profileImageUrl")
    EmployeeLookupResponse toResponse(EmployeeLookupVO vo);

    List<EmployeeLookupResponse> toResponseList(List<EmployeeLookupVO> voList);
}