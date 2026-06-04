package com.mycrewsoft.domain.task.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.task.mapper.AdminTaskScopeMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTaskScopeServiceImpl implements AdminTaskScopeService {

    private final AuthorizationService authorizationService;
    private final AdminTaskScopeMapper adminTaskScopeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AdminScopeOptionResponseDTO> getTaskScopeOptions() {
        assertRoleManagePermission();
        return adminTaskScopeMapper.selectTaskScopeOptions();
    }

    private void assertRoleManagePermission() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.ADMIN)
                .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.ADMIN_ROLE_MANAGE, resource);
    }
}
