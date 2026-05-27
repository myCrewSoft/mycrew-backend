package com.mycrewsoft.domain.employee.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.employee.dto.request.EmployeeRegisterRequest;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.employee.vo.EmployeeVO;
import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.PermissionCode;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
	private final DtoMapper DtoMapper;
	private final AuthorizationService authorizationService;
	
	
	private final EmployeeMapper employeeMapper;
	private final PasswordEncoder passwordEncoder;
	
	/**
	 * 관리자가 사원을 등록하는 서비스 메서드
	 */
	@Override
	@Transactional
	public void registerEmployee(EmployeeRegisterRequest employeeRequest) {
		// 1. 사원 ID 중복 확인
		EmployeeVO existEmployee = employeeMapper.selectEmployeeById(employeeRequest.getEmpId());
		
		// 2. 중복된 사원 ID가 존재하면 예외 발생
		if (existEmployee != null) {
			throw new CustomException(ErrorCode.DUPLICATE_EMPLOYEEID);
		}
		
		// 3. DTO를 VO로 변환
		EmployeeVO employee = DtoMapper.toDto(employeeRequest, EmployeeVO.class);
		String encodedPassword = passwordEncoder.encode(employee.getEmpId().toString());
		
		employee.setEnabled("Y");
		employee.setPswd(encodedPassword);
		employee.setFrstRegDt(LocalDateTime.now());
		employee.setEmpStatCd(EmpStatCode.EMP_INITIAL.getCode());
		
		// 4. 권한 체크 - 현재 사용자가 사원 등록 권한이 있는지 확인
		ResourceContext resource = ResourceContext.builder()
	            .resourceType(ResourceType.EMPLOYEE)
	            .build();

	    authorizationService.assertCurrentUserPermission(
	            PermissionCode.EMPLOYEE_CREATE,
	            resource
	    );
	    
	    // 5. 사원 정보 저장
	    employeeMapper.insertEmployee(employee);
	    
	    
	}

	@Override
	public void updateEmployee() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteEmployee() {
		// TODO Auto-generated method stub

	}

	@Override
	public EmployeeVO getEmployeeById() {
		// TODO Auto-generated method stub
		return null;
	}

}
