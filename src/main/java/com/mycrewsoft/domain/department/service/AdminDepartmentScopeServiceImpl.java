package com.mycrewsoft.domain.department.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.domain.department.mapper.AdminDepartmentScopeMapper;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDepartmentScopeServiceImpl implements AdminDepartmentScopeService {

    private final AuthorizationService authorizationService;
    private final AdminDepartmentScopeMapper adminDepartmentScopeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AdminScopeOptionResponseDTO> getDepartmentScopeOptions() {
        assertRoleManagePermission();
        return adminDepartmentScopeMapper.selectDepartmentScopeOptions();
    }

    private void assertRoleManagePermission() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.ADMIN)
                .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.ADMIN_ROLE_MANAGE, resource);
    }
}
