package com.mycrewsoft.domain.roleassignment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.Constants;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.roleassignment.mapper.RoleAssignmentMapper;
import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;
import com.mycrewsoft.security.authz.ScopeType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleAssignmentServiceImpl implements RoleAssignmentService {

	private final RoleAssignmentMapper roleAssignmentMapper;

	@Override
	@Transactional
	public void assignDefaultEmployeeRole(Long empId) {
		if (empId == null) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
		}

		Long roleId = roleAssignmentMapper.selectRoleIdByRoleCode(Constants.ROLE_EMPLOYEE_SELF);
		if (roleId == null) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		RoleAssignmentVO roleAssignment = new RoleAssignmentVO();
		roleAssignment.setRoleId(roleId);
		roleAssignment.setEmpId(empId);
		roleAssignment.setScopeTypeCd(ScopeType.SELF.name());
		roleAssignment.setScopeId(String.valueOf(empId));
		roleAssignment.setEnabled("Y");

		roleAssignmentMapper.insertRoleAssignmentIfAbsent(roleAssignment);
	}
}
