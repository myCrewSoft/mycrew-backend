package com.mycrewsoft.domain.employee.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper {
	String selectEmpDeptCodeByEmpId(Long empId);
}
