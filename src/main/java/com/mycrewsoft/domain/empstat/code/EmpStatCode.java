package com.mycrewsoft.domain.empstat.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 직원 상태 코드를 정의하는 열거형 클래스
 */
@Getter
@RequiredArgsConstructor
public enum EmpStatCode {
    EMP_INITIAL("EMP_INITIAL", "계정 등록 단계"),
    EMP_ACTIVE("EMP_ACTIVE", "정상 재직"),
    EMP_INACTIVE("EMP_INACTIVE", "비활성"),
    EMP_RETIRED("EMP_RETIRED", "퇴사"),
	EMP_VACATION("EMP_VACATION", "휴가");
    
	private final String code;
    private final String label;
}