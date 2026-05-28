package com.mycrewsoft.domain.employee.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.employee.vo.EmployeeVO;

@Mapper
public interface AdminEmployeeMapper {
	/**
	 * 관리자의 사원 등록을 위한 데이터베이스 삽입 메서드
	 * @param employee
	 */
	void insertEmployee(EmployeeVO employee);
	
	EmployeeVO selectEmployeeById(Long empId);
}
