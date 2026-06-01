package com.mycrewsoft.domain.employee.service;

import org.springframework.data.domain.Page;

import com.mycrewsoft.domain.employee.dto.request.EmployeeRegisterRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.EmployeeSearchDTO;
import com.mycrewsoft.domain.employee.dto.request.EmployeeStatusUpdateRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeDetailDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeListDTO;
import com.mycrewsoft.domain.employee.vo.EmployeeVO;

/**
 * 관리자용 사원 관리 서비스 인터페이스
 */
public interface AdminEmployeeService {
	 /**
	  * 사원 정보를 등록하는 메서드
	  * @param EmployeeRegisterRequestDTO 
	  */
	 void registerEmployee(EmployeeRegisterRequestDTO employeeRequest);

	 /**
	  * 사원 상태 정보를 수정하는 메서드
	  * @param 수정할 사원의 ID
	  */
	 void updateEmployeeStatus(Long empId, EmployeeStatusUpdateRequestDTO request);

	 /**
	  * 사원 정보를 삭제하는 메서드
	  * @param mbrId 삭제할 사원의 ID
	  */
	 void deleteEmployee();

	 /**
	  * 사원 정보를 조회하는 메서드
	  * @param mbrId 조회할 사원의 ID
	  * @return EmployeeVO 
	  */
	 EmployeeVO getEmployeeById();
	 
	/**
	 * 관리자가 사원 목록을 검색하는 메서드. 검색 조건에 따라 사원 목록을 페이지 형태로 반환한다.
	 * @param EmployeeSearchDTO condition
	 * @return Page<EmployeeListDTO>
	 */
	 Page<EmployeeListDTO> getEmployees(EmployeeSearchDTO condition);
	 
	 EmployeeDetailDTO getEmployeeDetailById(Long empId);
}
