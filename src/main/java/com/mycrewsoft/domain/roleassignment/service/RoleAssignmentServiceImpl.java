package com.mycrewsoft.domain.roleassignment.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

	@Override
	@Transactional
	public void syncDeptScopeChange(Map<Long, String> oldDeptCdByEmpId, String newDeptCd) {
		if (CollectionUtils.isEmpty(oldDeptCdByEmpId) || !StringUtils.hasText(newDeptCd)) {
			return;
		}

		String targetDeptCd = newDeptCd.trim();
		Long basicRoleId = null; // 신규 배정 시에만 조회 (지연 로딩)

		for (Map.Entry<Long, String> entry : oldDeptCdByEmpId.entrySet()) {
			Long empId = entry.getKey();
			if (empId == null) {
				continue;
			}

			String oldDeptCd = entry.getValue() == null ? null : entry.getValue().trim();

			if (!StringUtils.hasText(oldDeptCd)) {
				// [신규 배정] 이전 부서가 없던 사원 -> 새 부서 게시판 CRUD 범위를 자동 생성한다.
				// 현행 수동 흐름과 동일하게 기본 사원 역할(ROLE_EMPLOYEE_SELF)을 DEPT 범위로 부여한다.
				// (insertRoleAssignmentIfAbsent 가 중복을 막아 멱등하게 동작한다.)
				if (basicRoleId == null) {
					basicRoleId = roleAssignmentMapper.selectRoleIdByRoleCode(Constants.ROLE_EMPLOYEE_SELF);
					if (basicRoleId == null) {
						throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
					}
				}
				RoleAssignmentVO assignment = new RoleAssignmentVO();
				assignment.setRoleId(basicRoleId);
				assignment.setEmpId(empId);
				assignment.setScopeTypeCd(ScopeType.DEPT.name());
				assignment.setScopeId(targetDeptCd);
				assignment.setEnabled("Y");
				roleAssignmentMapper.insertRoleAssignmentIfAbsent(assignment);
			} else if (!oldDeptCd.equals(targetDeptCd)) {
				// [부서 이동] 이전 부서와 SCOPE_ID 가 일치하는 DEPT 범위만 새 부서로 재지정한다.
				// 다른 부서로 명시 배정된 DEPT 범위(예: 타부서 게시판 관리자)는 SCOPE_ID 가 달라 보존된다.
				roleAssignmentMapper.repointDeptScope(empId, oldDeptCd, targetDeptCd);
			}
			// oldDeptCd == targetDeptCd : 동일 부서 재배정이므로 변경 없음
		}
	}
}
