package com.mycrewsoft.security.rbac;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.security.authz.ScopedPermission;

/**
 * RBAC 권한 정보를 조회하고 권한 버전을 갱신하는 MyBatis Mapper.
 * 실제 SQL은 src/main/resources/mapper/security/RbacMapper.xml 에 정의한다.
 *
 * 역할:
 * - 사용자의 scoped permission 목록을 조회한다.
 * - 사용자의 현재 권한 버전을 조회한다.
 * - 권한 변경 시 권한 버전을 증가시킨다.
 * - 특정 역할을 가진 사용자 목록을 조회한다.
 *
 * 설계 기준:
 * - 역할 부여 정보는 TB_ROLE_ASSIGNMENT를 기준으로 조회한다.
 * - 역할과 권한의 연결은 TB_PERMISSION_ROLE_MAPPING을 기준으로 조회한다.
 */
@Mapper
public interface RbacMapper {

    List<ScopedPermission> selectScopedPermissionsByEmpId(@Param("empId") Long empId);

    Integer selectAuthVersionByEmpId(@Param("empId") Long empId);

    List<String> selectAuthoritiesByEmpId(@Param("empId") Long empId);

    int incrementAuthVersionForEmpId(@Param("empId") Long empId);

    List<Long> selectEnabledEmpIdsByRoleId(@Param("roleId") Long roleId);
}
