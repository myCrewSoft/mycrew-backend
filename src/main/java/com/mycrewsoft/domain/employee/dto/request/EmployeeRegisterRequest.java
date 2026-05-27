package com.mycrewsoft.domain.employee.dto.request;

import java.time.LocalDate;

import com.mycrewsoft.validate.constraints.PhoneNumber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 사원 등록을 위한 DTO 클래스. 관리자가 사원을 등록할 때 필요한 정보를 담는다.
 */
@Getter
@Setter
public class EmployeeRegisterRequest {
	@NotNull(message = "사번은 필수 입력값입니다.")
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
    
    @NotNull(message = "입사일자는 필수 입력값입니다.")
    private LocalDate entcoYmd;  // 입사일자
}