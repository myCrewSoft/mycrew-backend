package com.mycrewsoft.domain.project.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.project.mapper.AdminProjectScopeMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProjectScopeServiceImpl implements AdminProjectScopeService {

    private final AuthorizationService authorizationService;
    private final AdminProjectScopeMapper adminProjectScopeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AdminScopeOptionResponseDTO> getProjectScopeOptions() {
        assertRoleManagePermission();
        return adminProjectScopeMapper.selectProjectScopeOptions();
    }

    private void assertRoleManagePermission() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.ADMIN)
                .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.ADMIN_ROLE_MANAGE, resource);
    }
}
