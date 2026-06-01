package com.mycrewsoft.domain.employee.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.mapper.AdminEmployeeMapper;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.roleassignment.service.RoleAssignmentService;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {
	
	private final EmployeeMapper employeeMapper;
	
	@Override
	@Transactional(readOnly = true)
	public EmployeeProfileDTO getEmployeeProfile() {
		Long empId = SecurityUtil.getCurrentEmpId();
		
		if(employeeMapper.existsByEmpId(empId)) {
			EmployeeProfileDTO profile = employeeMapper.selectEmployeeProfileByEmpId(empId);
			return profile;
		} else {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}
	}

}
