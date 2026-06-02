package com.mycrewsoft.domain.employee.service;

import java.util.List;

import com.mycrewsoft.domain.employee.dto.request.RoleAssignRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleCreateRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleDeleteRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleRevokeRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleUpdateRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.PermissionResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleDetailResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleListResponseDTO;

public interface AdminAuthorizationService {

    List<PermissionResponseDTO> getPermissions();

    List<RoleListResponseDTO> getRoles();

    RoleDetailResponseDTO getRole(Long roleId);

    RoleDetailResponseDTO createRole(RoleCreateRequestDTO request);

    RoleDetailResponseDTO updateRole(Long roleId, RoleUpdateRequestDTO request);

    void deleteRole(Long roleId, RoleDeleteRequestDTO request);

    RoleDetailResponseDTO assignRole(Long roleId, RoleAssignRequestDTO request);

    RoleDetailResponseDTO revokeRole(Long roleId, RoleRevokeRequestDTO request);
}
