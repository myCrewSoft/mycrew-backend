package com.mycrewsoft.domain.employee.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;

@Mapper
public interface EmployeeMapper {
	/**
	 * 사번으로 사원의 부서코드를 조회하는 메서드
	 * @param empId
	 * @return deptCd
	 */
	String selectEmpDeptCodeByEmpId(Long empId);
	
	/**
	 * 사번으로 사원이 존재하는지 여부를 반환하는 메서드
	 * @param empId
	 * @return boolean
	 */
	boolean existsByEmpId(Long empId);
	
	/**
	 * 사번으로 사원의 간략한 정보를 조회하는 메서드
	 * @param empId
	 * @return EmployeeProfileDTO
	 */
	EmployeeProfileDTO selectEmployeeProfileByEmpId(Long empId);
}
