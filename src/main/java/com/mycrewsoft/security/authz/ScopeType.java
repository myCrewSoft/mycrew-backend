package com.mycrewsoft.security.authz;

/**
 * 역할이 적용되는 범위 유형을 정의하는 enum.
 *
 * 역할:
 * - GLOBAL, DEPT, PROJECT, SELF 같은 권한 범위를 코드로 표현한다.
 * - TB_ROLE_ASSIGNMENT.SCOPE_TYPE_CD 값과 매칭된다.
 *
 * 의미:
 * - GLOBAL: 전체 시스템 범위
 * - DEPT: 특정 부서 범위
 * - PROJECT: 특정 프로젝트 범위
 * - SELF: 본인 소유 리소스 범위
 */
public enum ScopeType {
    GLOBAL,
    DEPT,
    PROJECT,
    SELF
}
