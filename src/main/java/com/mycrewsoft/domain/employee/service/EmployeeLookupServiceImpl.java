package com.mycrewsoft.domain.employee.service;

import com.mycrewsoft.domain.employee.dto.request.EmployeeLookupRequest;
import com.mycrewsoft.domain.employee.dto.response.EmployeeLookupResponse;
import com.mycrewsoft.domain.employee.mapper.EmployeeLookupDtoMapper;
import com.mycrewsoft.domain.employee.mapper.EmployeeLookupMapper;
import com.mycrewsoft.domain.employee.vo.EmployeeLookupVO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeLookupServiceImpl implements EmployeeLookupService {

    private final EmployeeLookupMapper employeeLookupMapper;
    private final EmployeeLookupDtoMapper employeeLookupDtoMapper;

    @Override
    public List<EmployeeLookupResponse> lookupEmployees(EmployeeLookupRequest request) {
        List<EmployeeLookupVO> voList = employeeLookupMapper.selectEmployeesForLookup(request);
        return employeeLookupDtoMapper.toResponseList(voList);
    }
}