package com.mycrewsoft.domain.roleassignment.service;

public interface RoleAssignmentService {

	void assignDefaultEmployeeRole(Long empId);

	/**
	 * 부서 변경(이동/배정)에 따라 대상 사원들의 DEPT 권한 범위를 동기화한다.
	 *
	 * - 이전 부서가 있는 사원: 이전 부서와 일치하는 DEPT 범위를 새 부서로 재지정한다.
	 *   (다른 부서로 명시 배정된 DEPT 범위는 보존된다.)
	 * - 이전 부서가 없던(신규 배정) 사원: 새 부서 게시판 CRUD 범위를 기본 사원 역할로 자동 생성한다.
	 *
	 * @param oldDeptCdByEmpId 사원 ID -> 변경 전 부서코드 (없으면 null)
	 * @param newDeptCd        변경 후 부서코드
	 */
	void syncDeptScopeChange(java.util.Map<Long, String> oldDeptCdByEmpId, String newDeptCd);
}
