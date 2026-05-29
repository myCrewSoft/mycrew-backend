package com.mycrewsoft.domain.employee.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.employee.dto.request.EmployeeSearchDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeDetailDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeListDTO;
import com.mycrewsoft.domain.employee.vo.EmployeeVO;

@Mapper
public interface AdminEmployeeMapper {
	/**
	 * 관리자의 사원 등록을 위한 데이터베이스 삽입 메서드
	 * @param employee
	 */
	void insertEmployee(EmployeeVO employee);
	
	/**
	 * 사번으로 사원 정보를 조회하는 메서드.
	 * 간단한 정보만 들어있다.
	 * @param Long empId
	 * @return EmployeeVO
	 */
	EmployeeVO selectEmployeeById(Long empId);

	/**
	 * 사번으로 사원 상세 정보를 조회하는 메서드.
	 * 사원 기본 정보 + 부서명, 직급명, 사원 상태명, 역할, 이메일 등이 들어있다.
	 * @param Long empId
	 * @return EmployeeDetailDTO 
	 */
	EmployeeDetailDTO selectEmployeeDetailById(Long empId);
	
	/**
	 * 관리자가 사원 목록을 검색할 때, 검색 조건에 맞는 사원의 총 수를 반환하는 메서드
	 * @param EmployeeSearchDTO condition
	 * @return long
	 */
	long countEmployees(@Param("condition") EmployeeSearchDTO condition);

	/**
	 * 관리자가 사원 목록을 검색할 때, 검색 조건에 맞는 사원들의 목록을 반환하는 메서드
	 * @param EmployeeSearchDTO condition
	 * @param int offset
	 * @param int size
	 * @return List<EmployeeListDTO>
	 */
	List<EmployeeListDTO> selectEmployees(
	        @Param("condition") EmployeeSearchDTO condition,
	        @Param("offset") int offset,
	        @Param("size") int size
	);
}
