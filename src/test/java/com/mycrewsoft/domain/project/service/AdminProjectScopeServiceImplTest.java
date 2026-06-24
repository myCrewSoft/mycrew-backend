// package com.mycrewsoft.domain.project.service;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.List;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.mycrewsoft.common.constant.PermissionCode;
// import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
// import com.mycrewsoft.domain.project.mapper.AdminProjectScopeMapper;
// import com.mycrewsoft.security.authz.AuthorizationService;
// import com.mycrewsoft.security.authz.ResourceContext;

// @ExtendWith(MockitoExtension.class)
// class AdminProjectScopeServiceImplTest {

//     @Mock
//     private AuthorizationService authorizationService;

//     @Mock
//     private AdminProjectScopeMapper mapper;

//     @Test
//     void getProjectScopeOptionsChecksRoleManagePermissionAndReturnsOptions() {
//         AdminScopeOptionResponseDTO option = new AdminScopeOptionResponseDTO("100", "100", "Migration");
//         when(mapper.selectProjectScopeOptions()).thenReturn(List.of(option));

        AdminProjectScopeServiceImpl service = new AdminProjectScopeServiceImpl(authorizationService, mapper, null);

//         List<AdminScopeOptionResponseDTO> options = service.getProjectScopeOptions();

//         assertThat(options).containsExactly(option);
//         verify(authorizationService).assertCurrentUserPermission(eq(PermissionCode.ADMIN_ROLE_MANAGE), any(ResourceContext.class));
//     }
// }
