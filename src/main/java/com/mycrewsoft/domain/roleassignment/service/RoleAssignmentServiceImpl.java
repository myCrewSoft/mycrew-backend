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

        for (Map.Entry<Long, String> entry : oldDeptCdByEmpId.entrySet()) {
            Long empId = entry.getKey();
            if (empId == null) {
                continue;
            }

            String oldDeptCd = entry.getValue() == null ? null : entry.getValue().trim();
            if (!StringUtils.hasText(oldDeptCd)) {
                continue;
            }

            if (!oldDeptCd.equals(targetDeptCd)) {
                roleAssignmentMapper.repointDeptScope(empId, oldDeptCd, targetDeptCd);
            }
        }
    }
}
