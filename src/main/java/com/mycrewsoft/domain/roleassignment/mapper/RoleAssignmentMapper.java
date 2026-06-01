package com.mycrewsoft.domain.roleassignment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;

@Mapper
public interface RoleAssignmentMapper {

	Long selectRoleIdByRoleCode(@Param("roleCd") String roleCd);

	List<RoleAssignmentVO> selectRoleAssignmentsByEmpId(@Param("empId") Long empId);

	int insertRoleAssignmentIfAbsent(RoleAssignmentVO roleAssignment);
}
