package com.mycrewsoft.domain.employee.dto.request;

import java.time.LocalDate;

import com.mycrewsoft.validate.constraints.PhoneNumber;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeRegisterRequest {
	@NotBlank(message = "사번은 필수 입력값입니다.")
    private Long empId;          // 사번, 로그인 ID로도 사용
	
	@NotBlank(message = "사원명은 필수 입력값입니다.")
    private String empNm;        // 사원명
	
	@NotBlank(message = "주민등록번호는 필수 입력값입니다.")
    private String rrno;         // 주민등록번호
	
	@NotBlank(message = "성별은 필수 입력값입니다.")
    private String genderCd;     // 성별
    
    @NotBlank(message = "휴대폰 번호는 필수 입력값입니다.")
    @PhoneNumber
    private String mblTelno;     // 휴대폰 번호
    
    @NotBlank(message = "우편번호는 필수 입력값입니다.")
    private String zip;          // 우편번호
    
    @NotBlank(message = "주소는 필수 입력값입니다.")
    private String addr;         // 주소
    
    @NotBlank(message = "입사일자는 필수 입력값입니다.")
    private LocalDate entcoYmd;  // 입사일자
}