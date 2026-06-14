// package com.mycrewsoft.domain.employee.service;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.inOrder;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.List;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InOrder;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.mycrewsoft.common.constant.Constants;
// import com.mycrewsoft.common.constant.PermissionCode;
// import com.mycrewsoft.common.exception.CustomException;
// import com.mycrewsoft.common.exception.ErrorCode;
// import com.mycrewsoft.domain.employee.dto.request.PermissionStatusUpdateRequestDTO;
// import com.mycrewsoft.domain.employee.dto.request.RoleAssignRequestDTO;
// import com.mycrewsoft.domain.employee.dto.request.RoleCreateRequestDTO;
// import com.mycrewsoft.domain.employee.dto.request.RoleDeleteRequestDTO;
// import com.mycrewsoft.domain.employee.dto.request.RoleRevokeRequestDTO;
// import com.mycrewsoft.domain.employee.dto.request.RoleUpdateRequestDTO;
// import com.mycrewsoft.domain.employee.dto.response.PermissionResponseDTO;
// import com.mycrewsoft.domain.employee.dto.response.RoleDetailResponseDTO;
// import com.mycrewsoft.domain.employee.mapper.AdminAuthorizationMapper;
// import com.mycrewsoft.domain.role.vo.RoleVO;
// import com.mycrewsoft.security.authz.AuthorizationService;
// import com.mycrewsoft.security.authz.ResourceContext;
// import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;

// @ExtendWith(MockitoExtension.class)
// class AdminAuthorizationServiceImplTest {

//     @Mock
//     private AuthorizationService authorizationService;

//     @Mock
//     private AdminAuthorizationMapper adminAuthorizationMapper;

//     @Mock
//     private RbacAuthorizationChangeService rbacAuthorizationChangeService;

//     @Test
//     void getPermissionsChecksRoleManagePermissionAndReturnsPermissions() {
//         PermissionResponseDTO permission = new PermissionResponseDTO();
//         permission.setPermissionId(1L);
//         permission.setPermissionCode("ROLE_MANAGE");
//         permission.setPermissionName("ROLE_MANAGE");
//         permission.setDescription("Manage roles");
//         permission.setEnabled("Y");
//         when(adminAuthorizationMapper.selectPermissions()).thenReturn(List.of(permission));

//         AdminAuthorizationServiceImpl service = service();

//         List<PermissionResponseDTO> permissions = service.getPermissions();

//         assertThat(permissions).containsExactly(permission);
//         verify(authorizationService).assertCurrentUserPermission(
//                 eq(PermissionCode.ADMIN_ROLE_MANAGE),
//                 any(ResourceContext.class));
//     }

//     @Test
//     void createRoleRejectsEmptyPermissionIds() {
//         RoleCreateRequestDTO request = new RoleCreateRequestDTO();
//         request.setRoleCode("ROLE_CUSTOM_MANAGER");
//         request.setRoleName("Manager");
//         request.setPermissionIds(List.of());

//         AdminAuthorizationServiceImpl service = service();

//         assertThatThrownBy(() -> service.createRole(request))
//                 .isInstanceOf(CustomException.class)
//                 .extracting("errorCode")
//                 .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

//         verify(adminAuthorizationMapper, never()).insertRole(any(RoleVO.class));
//     }

//     @Test
//     void createRoleRejectsDuplicateRoleCode() {
//         RoleCreateRequestDTO request = roleCreateRequest();
//         when(adminAuthorizationMapper.selectRoleIdByRoleCode("ROLE_CUSTOM_MANAGER")).thenReturn(10L);

//         AdminAuthorizationServiceImpl service = service();

//         assertThatThrownBy(() -> service.createRole(request))
//                 .isInstanceOf(CustomException.class)
//                 .extracting("errorCode")
//                 .isEqualTo(ErrorCode.DUPLICATE_ROLE_CODE);

//         verify(adminAuthorizationMapper, never()).insertRole(any(RoleVO.class));
//     }

//     @Test
//     void updateRoleReplacesPermissionMappingsAndRefreshesAssignedEmployees() {
//         Long roleId = 10L;
//         RoleUpdateRequestDTO request = new RoleUpdateRequestDTO();
//         request.setRoleName("Updated Manager");
//         request.setDescription("Updated description");
//         request.setPermissionIds(List.of(1L, 2L));
//         RoleDetailResponseDTO existingRole = new RoleDetailResponseDTO();
//         existingRole.setRoleId(roleId);
//         existingRole.setRoleCode("ROLE_CUSTOM_MANAGER");
//         when(adminAuthorizationMapper.selectRoleDetail(roleId)).thenReturn(existingRole);
//         when(adminAuthorizationMapper.countEnabledPermissionsByIds(request.getPermissionIds())).thenReturn(2);

//         AdminAuthorizationServiceImpl service = service();

//         service.updateRole(roleId, request);

//         InOrder order = inOrder(adminAuthorizationMapper, rbacAuthorizationChangeService);
//         order.verify(adminAuthorizationMapper).updateRole(roleId, request.getRoleName(), request.getDescription(), null);
//         order.verify(adminAuthorizationMapper).deletePermissionMappingsByRoleId(roleId);
//         order.verify(adminAuthorizationMapper).insertPermissionMappings(roleId, request.getPermissionIds());
//         order.verify(rbacAuthorizationChangeService).refreshRolePermissions(roleId);
//     }

//     @Test
//     void deleteRoleRejectsProtectedSystemRole() {
//         RoleDetailResponseDTO superAdminRole = new RoleDetailResponseDTO();
//         superAdminRole.setRoleId(1L);
//         superAdminRole.setRoleCode(Constants.SUPER_ADMIN);
//         when(adminAuthorizationMapper.selectRoleDetail(1L)).thenReturn(superAdminRole);

//         AdminAuthorizationServiceImpl service = service();

//         assertThatThrownBy(() -> service.deleteRole(1L, new RoleDeleteRequestDTO()))
//                 .isInstanceOf(CustomException.class)
//                 .extracting("errorCode")
//                 .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

//         verify(adminAuthorizationMapper, never()).deleteRole(1L);
//     }

//     @Test
//     void deleteRoleWithAssignedEmployeesRequiresReplacementRole() {
//         RoleDetailResponseDTO role = new RoleDetailResponseDTO();
//         role.setRoleId(10L);
//         role.setRoleCode("ROLE_CUSTOM_MANAGER");
//         when(adminAuthorizationMapper.selectRoleDetail(10L)).thenReturn(role);
//         when(adminAuthorizationMapper.selectEnabledEmpIdsByRoleId(10L)).thenReturn(List.of(20260001L));

//         AdminAuthorizationServiceImpl service = service();

//         assertThatThrownBy(() -> service.deleteRole(10L, new RoleDeleteRequestDTO()))
//                 .isInstanceOf(CustomException.class)
//                 .extracting("errorCode")
//                 .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

//         verify(adminAuthorizationMapper, never()).deleteRole(10L);
//     }

//     @Test
//     void deleteRoleTransfersAssignmentsDeletesRoleAndRefreshesAffectedEmployees() {
//         RoleDetailResponseDTO role = new RoleDetailResponseDTO();
//         role.setRoleId(10L);
//         role.setRoleCode("ROLE_CUSTOM_MANAGER");
//         RoleDetailResponseDTO replacement = new RoleDetailResponseDTO();
//         replacement.setRoleId(20L);
//         replacement.setRoleCode("ROLE_CUSTOM_REPLACEMENT");
//         RoleDeleteRequestDTO request = new RoleDeleteRequestDTO();
//         request.setReplacementRoleId(20L);
//         List<Long> affectedEmpIds = List.of(20260001L, 20260002L);

//         when(adminAuthorizationMapper.selectRoleDetail(10L)).thenReturn(role);
//         when(adminAuthorizationMapper.selectEnabledEmpIdsByRoleId(10L)).thenReturn(affectedEmpIds);
//         when(adminAuthorizationMapper.selectRoleDetail(20L)).thenReturn(replacement);

//         AdminAuthorizationServiceImpl service = service();

//         service.deleteRole(10L, request);

//         InOrder order = inOrder(adminAuthorizationMapper, rbacAuthorizationChangeService);
//         order.verify(adminAuthorizationMapper).insertReplacementRoleAssignments(10L, 20L);
//         order.verify(adminAuthorizationMapper).deleteRoleAssignmentsByRoleId(10L);
//         order.verify(adminAuthorizationMapper).deletePermissionMappingsByRoleId(10L);
//         order.verify(adminAuthorizationMapper).deleteRole(10L);
//         order.verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(affectedEmpIds);
//     }

//     @Test
//     void assignRoleToEmployeesCreatesAssignmentsAndRefreshesEmployees() {
//         Long roleId = 10L;
//         RoleAssignRequestDTO request = new RoleAssignRequestDTO();
//         request.setEmpIds(List.of(20260001L, 20260002L));
//         request.setScopeTypeCd("GLOBAL");
//         RoleDetailResponseDTO role = new RoleDetailResponseDTO();
//         role.setRoleId(roleId);
//         role.setRoleCode("ROLE_CUSTOM_MANAGER");
//         when(adminAuthorizationMapper.selectRoleDetail(roleId)).thenReturn(role);
//         when(adminAuthorizationMapper.countEnabledEmployeesByIds(request.getEmpIds())).thenReturn(2);

//         AdminAuthorizationServiceImpl service = service();

//         service.assignRole(roleId, request);

//         verify(adminAuthorizationMapper).insertRoleAssignments(roleId, request.getEmpIds(), "GLOBAL", "*");
//         verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(request.getEmpIds());
//         verify(authorizationService).assertCurrentUserPermission(
//                 eq(PermissionCode.ADMIN_ROLE_MANAGE),
//                 any(ResourceContext.class));
//     }

//     @Test
//     void assignRoleRejectsMissingEmployees() {
//         Long roleId = 10L;
//         RoleAssignRequestDTO request = new RoleAssignRequestDTO();
//         request.setEmpIds(List.of(20260001L, 20260002L));
//         request.setScopeTypeCd("GLOBAL");
//         RoleDetailResponseDTO role = new RoleDetailResponseDTO();
//         role.setRoleId(roleId);
//         role.setRoleCode("ROLE_CUSTOM_MANAGER");
//         when(adminAuthorizationMapper.selectRoleDetail(roleId)).thenReturn(role);
//         when(adminAuthorizationMapper.countEnabledEmployeesByIds(request.getEmpIds())).thenReturn(1);

//         AdminAuthorizationServiceImpl service = service();

//         assertThatThrownBy(() -> service.assignRole(roleId, request))
//                 .isInstanceOf(CustomException.class)
//                 .extracting("errorCode")
//                 .isEqualTo(ErrorCode.USER_NOT_FOUND);

//         verify(adminAuthorizationMapper, never()).insertRoleAssignments(any(), any(), any(), any());
//     }

//     @Test
//     void revokeRoleFromEmployeesDeletesAssignmentsAndRefreshesEmployees() {
//         Long roleId = 10L;
//         RoleRevokeRequestDTO request = new RoleRevokeRequestDTO();
//         request.setEmpIds(List.of(20260001L, 20260002L));
//         RoleDetailResponseDTO role = new RoleDetailResponseDTO();
//         role.setRoleId(roleId);
//         role.setRoleCode("ROLE_CUSTOM_MANAGER");
//         when(adminAuthorizationMapper.selectRoleDetail(roleId)).thenReturn(role);

//         AdminAuthorizationServiceImpl service = service();

//         service.revokeRole(roleId, request);

//         verify(adminAuthorizationMapper).deleteRoleAssignments(roleId, request.getEmpIds(), null, null);
//         verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(request.getEmpIds());
//     }

//     @Test
//     void updatePermissionStatusUpdatesEnabledFlagAndRefreshesMappedRoles() {
//         PermissionStatusUpdateRequestDTO request = new PermissionStatusUpdateRequestDTO();
//         request.setEnabled("N");
//         PermissionResponseDTO permission = new PermissionResponseDTO();
//         permission.setPermissionId(1L);
//         permission.setPermissionCode("ADMIN_CONSOLE_ACCESS");
//         when(adminAuthorizationMapper.selectPermissionById(1L)).thenReturn(permission);
//         when(adminAuthorizationMapper.selectRoleIdsByPermissionId(1L)).thenReturn(List.of(10L, 20L));

//         AdminAuthorizationServiceImpl service = service();

//         PermissionResponseDTO response = service.updatePermissionStatus(1L, request);

//         assertThat(response).isEqualTo(permission);
//         verify(adminAuthorizationMapper).updatePermissionEnabled(1L, "N");
//         verify(rbacAuthorizationChangeService).refreshRolePermissions(10L);
//         verify(rbacAuthorizationChangeService).refreshRolePermissions(20L);
//         verify(authorizationService).assertCurrentUserPermission(
//                 eq(PermissionCode.ADMIN_ROLE_MANAGE),
//                 any(ResourceContext.class));
//     }

//     @Test
//     void updatePermissionStatusRejectsUnknownEnabledValue() {
//         PermissionStatusUpdateRequestDTO request = new PermissionStatusUpdateRequestDTO();
//         request.setEnabled("YES");

//         AdminAuthorizationServiceImpl service = service();

//         assertThatThrownBy(() -> service.updatePermissionStatus(1L, request))
//                 .isInstanceOf(CustomException.class)
//                 .extracting("errorCode")
//                 .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

//         verify(adminAuthorizationMapper, never()).updatePermissionEnabled(any(), any());
//     }

//     private AdminAuthorizationServiceImpl service() {
//         return new AdminAuthorizationServiceImpl(
//                 authorizationService,
//                 adminAuthorizationMapper,
//                 rbacAuthorizationChangeService);
//     }

//     private RoleCreateRequestDTO roleCreateRequest() {
//         RoleCreateRequestDTO request = new RoleCreateRequestDTO();
//         request.setRoleCode("ROLE_CUSTOM_MANAGER");
//         request.setRoleName("Manager");
//         request.setDescription("Manager role");
//         request.setPermissionIds(List.of(1L, 2L));
//         return request;
//     }
// }
