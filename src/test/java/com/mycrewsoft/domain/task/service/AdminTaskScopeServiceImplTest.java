package com.mycrewsoft.domain.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.task.mapper.AdminTaskScopeMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;

@ExtendWith(MockitoExtension.class)
class AdminTaskScopeServiceImplTest {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AdminTaskScopeMapper mapper;

    @Test
    void getTaskScopeOptionsChecksRoleManagePermissionAndReturnsOptions() {
        AdminScopeOptionResponseDTO option = new AdminScopeOptionResponseDTO("200", "200", "API Work");
        when(mapper.selectTaskScopeOptions()).thenReturn(List.of(option));

        AdminTaskScopeServiceImpl service = new AdminTaskScopeServiceImpl(authorizationService, mapper);

        List<AdminScopeOptionResponseDTO> options = service.getTaskScopeOptions();

        assertThat(options).containsExactly(option);
        verify(authorizationService).assertCurrentUserPermission(eq(PermissionCode.ADMIN_ROLE_MANAGE), any(ResourceContext.class));
    }
}
