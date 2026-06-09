package com.mycrewsoft.security.authz;

/**
 * 인가 대상 리소스 유형을 정의하는 enum.
 *
 * 역할:
 * - 게시글, 프로젝트, 드라이브, 채팅방 등 보호 대상 도메인을 구분한다.
 * - AuthorizationService 호출 시 어떤 종류의 리소스를 검사하는지 명확히 표현한다.
 *
 * 주의:
 * - 권한 코드는 permissionCd로 판단한다.
 * - ResourceType은 인가 로그, 예외 메시지, 도메인 구분을 위한 보조 정보다.
 */
public enum ResourceType {
	ADMIN,
    BOARD,
    PROJECT,
    TASK,
    SCHEDULE,
    DRIVE,
    MAIL,
    APPROVAL,
    EMPLOYEE,
    DEPARTMENT
}
