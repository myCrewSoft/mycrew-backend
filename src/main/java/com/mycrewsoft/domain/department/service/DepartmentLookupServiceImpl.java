package com.mycrewsoft.domain.department.service;

import com.mycrewsoft.domain.department.dto.response.DepartmentLookupResponse;
import com.mycrewsoft.domain.department.mapper.DepartmentLookupDtoMapper;
import com.mycrewsoft.domain.department.mapper.DepartmentLookupMapper;
import com.mycrewsoft.domain.department.vo.DepartmentLookupVO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentLookupServiceImpl implements DepartmentLookupService {

    private final DepartmentLookupMapper departmentLookupMapper;
    private final DepartmentLookupDtoMapper departmentLookupDtoMapper;

    @Override
    public List<DepartmentLookupResponse> lookupDepartments() {
        List<DepartmentLookupVO> voList = departmentLookupMapper.selectDepartmentsForLookup();
        return departmentLookupDtoMapper.toResponseList(voList);
    }
}