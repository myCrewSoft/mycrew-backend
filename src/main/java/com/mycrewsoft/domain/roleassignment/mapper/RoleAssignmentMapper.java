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

	/**
	 * 부서 이동 시, 이전 부서와 일치하는 DEPT 범위의 역할 배정 SCOPE_ID를 새 부서로 갱신한다.
	 * SCOPE_ID가 이전 부서와 다른(명시적 타부서) DEPT 범위는 영향을 받지 않는다.
	 *
	 * @return 갱신된 행 수
	 */
	int repointDeptScope(
			@Param("empId") Long empId,
			@Param("oldDeptCd") String oldDeptCd,
			@Param("newDeptCd") String newDeptCd);
}
