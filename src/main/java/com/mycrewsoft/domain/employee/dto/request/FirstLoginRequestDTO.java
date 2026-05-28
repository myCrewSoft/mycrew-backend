package com.mycrewsoft.domain.employee.dto.request;

import java.time.LocalDate;

import com.mycrewsoft.validate.constraints.StrongPassword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 첫 로그인 처리를 위한 DTO 클래스. 사원이 계정에 첫 로그인 되었을 때를 처리한다.
 */
@Getter
@Setter
@Schema(description = "첫 로그인 요청 DTO")
public class FirstLoginRequestDTO {
	@NotBlank(message = "이메일 주소는 필수 입력값입니다.")
	@Schema(description = "이메일 주소", example = "example@goolge.com")
	String emailAddr; 	 // 이메일 주소
	
	@NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
	@Schema(description = "새 비밀번호", example = "ㄴㅇㅁㄴㅇㅇㄴ?")
	@StrongPassword
	String newPassword;  // 새 비밀번호
}
