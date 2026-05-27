package com.mycrewsoft.domain.employee.service;

import com.mycrewsoft.domain.employee.dto.request.EmployeeRegisterRequest;
import com.mycrewsoft.domain.employee.vo.EmployeeVO;

public interface EmployeeService {
	 /**
	  * 사원 정보를 등록하는 메서드
	  * @param employeeVO 등록할 사원 정보가 담긴 VO 객체
	  * @return 등록된 사원의 ID
	  */
	 void registerEmployee(EmployeeRegisterRequest employeeRequest);

	 /**
	  * 사원 정보를 수정하는 메서드
	  * @param employeeVO 수정할 사원 정보가 담긴 VO 객체
	  * @return 수정된 사원의 ID
	  */
	 void updateEmployee();

	 /**
	  * 사원 정보를 삭제하는 메서드
	  * @param mbrId 삭제할 사원의 ID
	  * @return 삭제된 사원의 ID
	  */
	 void deleteEmployee();

	 /**
	  * 사원 정보를 조회하는 메서드
	  * @param mbrId 조회할 사원의 ID
	  * @return 조회된 사원 정보가 담긴 VO 객체
	  */
	 EmployeeVO getEmployeeById();
}
