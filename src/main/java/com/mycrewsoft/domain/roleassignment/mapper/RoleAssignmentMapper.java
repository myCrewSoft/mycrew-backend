package com.mycrewsoft.domain.roleassignment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;

@Mapper
public interface RoleAssignmentMapper {

	Long selectRoleIdByRoleCode(@Param("roleCd") String roleCd);

	int insertRoleAssignmentIfAbsent(RoleAssignmentVO roleAssignment);
}
