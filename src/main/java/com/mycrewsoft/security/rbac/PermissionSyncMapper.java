package com.mycrewsoft.security.rbac;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * PermissionCode enum과 TB_PERMISSION 마스터 데이터를 동기화하는 MyBatis Mapper.
 * 실제 SQL은 src/main/resources/mapper/security/PermissionSyncMapper.xml 에 정의한다.
 *
 * 역할:
 * - enum에는 있지만 TB_PERMISSION에는 없는 권한 코드를 추가한다.
 * - 이미 존재하는 권한 코드는 수정하지 않는다.
 */
@Mapper
public interface PermissionSyncMapper {

    int mergePermissions(@Param("permissions") List<PermissionSeed> permissions);
}
