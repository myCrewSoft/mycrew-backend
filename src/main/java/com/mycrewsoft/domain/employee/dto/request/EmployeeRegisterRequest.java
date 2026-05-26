package com.mycrewsoft.domain.employee.dto.request;

import java.time.LocalDate;

public class EmployeeRegisterRequest {

    private Long empId;          // 사번, 로그인 ID로도 사용
    private String empNm;        // 사원명
    private String rrno;         // 주민등록번호
    private String genderCd;     // 성별
    private String mblTelno;     // 휴대폰 번호
    private String zip;          // 우편번호
    private String addr;         // 주소
    private LocalDate entcoYmd;  // 입사일자
}