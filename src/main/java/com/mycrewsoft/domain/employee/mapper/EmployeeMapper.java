package com.mycrewsoft.domain.employee.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper {
	/**
	 * 사번으로 사원의 부서코드를 조회하는 메서드
	 * @param empId
	 * @return deptCd
	 */
	String selectEmpDeptCodeByEmpId(Long empId);
}
