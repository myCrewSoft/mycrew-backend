package com.mycrewsoft.domain.employee.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.employee.dto.response.PermissionResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleDetailResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleEmployeeResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleListResponseDTO;
import com.mycrewsoft.domain.role.vo.RoleVO;

@Mapper
public interface AdminAuthorizationMapper {

    List<PermissionResponseDTO> selectPermissions();

    List<RoleListResponseDTO> selectRoles();

    RoleDetailResponseDTO selectRoleDetail(@Param("roleId") Long roleId);

    List<PermissionResponseDTO> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    List<RoleEmployeeResponseDTO> selectEmployeesByRoleId(@Param("roleId") Long roleId);

    Long selectRoleIdByRoleCode(@Param("roleCode") String roleCode);

    int countEnabledPermissionsByIds(@Param("permissionIds") List<Long> permissionIds);

    int insertRole(RoleVO role);

    int updateRole(
            @Param("roleId") Long roleId,
            @Param("roleName") String roleName,
            @Param("description") String description,
            @Param("lastModifierId") Long lastModifierId);

    int deleteRole(@Param("roleId") Long roleId);

    int deletePermissionMappingsByRoleId(@Param("roleId") Long roleId);

    int insertPermissionMappings(
            @Param("roleId") Long roleId,
            @Param("permissionIds") List<Long> permissionIds);

    List<Long> selectEnabledEmpIdsByRoleId(@Param("roleId") Long roleId);

    int insertReplacementRoleAssignments(
            @Param("roleId") Long roleId,
            @Param("replacementRoleId") Long replacementRoleId);

    int deleteRoleAssignmentsByRoleId(@Param("roleId") Long roleId);

    int countEnabledEmployeesByIds(@Param("empIds") List<Long> empIds);

    int insertRoleAssignments(
            @Param("roleId") Long roleId,
            @Param("empIds") List<Long> empIds,
            @Param("scopeTypeCd") String scopeTypeCd,
            @Param("scopeId") String scopeId);

    int deleteRoleAssignments(
            @Param("roleId") Long roleId,
            @Param("empIds") List<Long> empIds,
            @Param("scopeTypeCd") String scopeTypeCd,
            @Param("scopeId") String scopeId);
}
