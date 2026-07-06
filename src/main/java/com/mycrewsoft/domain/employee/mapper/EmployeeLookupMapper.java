package com.mycrewsoft.domain.employee.mapper;

import com.mycrewsoft.domain.employee.dto.request.EmployeeLookupRequest;
import com.mycrewsoft.domain.employee.vo.EmployeeLookupVO;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmployeeLookupMapper {

    List<EmployeeLookupVO> selectEmployeesForLookup(EmployeeLookupRequest request);
    
    List<Long> selectAllEmpIds();
}