package com.mycrewsoft.domain.roleassignment.service;

import java.util.Map;

public interface RoleAssignmentService {

    void assignDefaultEmployeeRole(Long empId);

    /**
     * 부서 변경에 따라 대상 사원들의 명시 DEPT 권한 범위를 동기화한다.
     *
     * 이전 부서가 있는 사원은 이전 부서와 일치하는 DEPT 범위만 새 부서로 재지정한다.
     * 이전 부서가 없던 신규 배정 사원에게는 기본 사원 역할의 DEPT 범위를 자동 생성하지 않는다.
     * 기본 사원의 자기 부서 접근은 권한 인가 엔진이 현재 부서 관계로 판단한다.
     *
     * @param oldDeptCdByEmpId 사원 ID -> 변경 전 부서 코드
     * @param newDeptCd 변경 후 부서 코드
     */
    void syncDeptScopeChange(Map<Long, String> oldDeptCdByEmpId, String newDeptCd);
}
