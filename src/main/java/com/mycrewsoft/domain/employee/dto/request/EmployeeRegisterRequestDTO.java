package com.mycrewsoft.domain.employee.dto.request;

import java.time.LocalDate;

import com.mycrewsoft.validate.constraints.PhoneNumber;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 사원 등록을 위한 DTO 클래스. 관리자가 사원을 등록할 때 필요한 정보를 담는다.
 */
@Getter
@Setter
@Schema(description = "사원 등록 요청 DTO")
public class EmployeeRegisterRequestDTO {
	@NotNull(message = "사번은 필수 입력값입니다.")
	@Schema(description = "사번", example = "01234567")
    private Long empId;          // 사번, 로그인 ID로도 사용
	
	@NotBlank(message = "사원명은 필수 입력값입니다.")
	@Schema(description = "사원명", example = "홍길동")
    private String empNm;        // 사원명
	
	@NotBlank(message = "주민등록번호는 필수 입력값입니다.")
	@Schema(description = "주민등록번호", example = "오늘의 일기")
    private String rrno;         // 주민등록번호
	
	@NotBlank(message = "성별은 필수 입력값입니다.")
	@Schema(description = "성별 코드", example = "M")
    private String genderCd;     // 성별
    
    @NotBlank(message = "휴대폰 번호는 필수 입력값입니다.")
    @PhoneNumber
    @Schema(description = "휴대폰 번호", example = "010-1234-5678")
    private String mblTelno;     // 휴대폰 번호
    
    @NotBlank(message = "우편번호는 필수 입력값입니다.")
    @Schema(description = "우편번호", example = "12345")
    private String zip;          // 우편번호
    
    @NotBlank(message = "주소는 필수 입력값입니다.")
    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    private String addr;         // 주소
    
    @NotNull(message = "입사일자는 필수 입력값입니다.")
    @Schema(description = "입사일자", example = "2024-01-01")
    private LocalDate entcoYmd;  // 입사일자
}