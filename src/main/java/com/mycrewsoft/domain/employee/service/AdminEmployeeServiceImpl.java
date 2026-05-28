package com.mycrewsoft.domain.employee.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.employee.dto.request.EmployeeRegisterRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.EmployeeSearchDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeListDTO;
import com.mycrewsoft.domain.employee.mapper.AdminEmployeeMapper;
import com.mycrewsoft.domain.employee.vo.EmployeeVO;
import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.domain.roleassignment.service.RoleAssignmentService;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.PermissionCode;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자용 사원 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class AdminEmployeeServiceImpl implements AdminEmployeeService {
	private final DtoMapper DtoMapper;
	private final AuthorizationService authorizationService;
	private final AdminEmployeeMapper adminEmployeeMapper;
	private final PasswordEncoder passwordEncoder;
	private final RoleAssignmentService roleAssignmentService;
	private final RbacAuthorizationChangeService rbacAuthorizationChangeService;
	
	/**
	 * 관리자가 사원을 등록하는 서비스 메서드
	 */
	@Override
	@Transactional
	public void registerEmployee(EmployeeRegisterRequestDTO employeeRequest) {
		// 1. 사원 ID 중복 확인
		EmployeeVO existEmployee = adminEmployeeMapper.selectEmployeeById(employeeRequest.getEmpId());
		
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
	    adminEmployeeMapper.insertEmployee(employee);
	    
	    // 6. 사원 등록 후 기본 사원 역할 부여
	    roleAssignmentService.assignDefaultEmployeeRole(employee.getEmpId());
	    	    
	    // 7. 역할 부여 후 버전 권한 생성
	    rbacAuthorizationChangeService.refreshEmployeePermissions(employee.getEmpId());
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

	@Override
	@Transactional(readOnly = true)
	public Page<EmployeeListDTO> getEmployees(EmployeeSearchDTO condition) {
		ResourceContext resource = ResourceContext.builder()
										        .resourceType(ResourceType.EMPLOYEE)
										        .build();
		
		authorizationService.assertCurrentUserPermission(
	            PermissionCode.EMPLOYEE_READ,
	            resource
	    );

	    int page = Math.max(condition.getPage(), 0);
	    int size = Math.min(Math.max(condition.getSize(), 1), 100);
	    int offset = page * size;

	    long total = adminEmployeeMapper.countEmployees(condition);

	    List<EmployeeListDTO> content =
	    		adminEmployeeMapper.selectEmployees(condition, offset, size);

	    Pageable pageable = PageRequest.of(page, size);

	    return new PageImpl<>(content, pageable, total);
	}

}
