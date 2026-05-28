package com.mycrewsoft.domain.employee.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 요청을 위한 DTO 클래스. 사용자가 로그인할 때 필요한 정보를 담는다. 사번(empId)과 비밀번호(password)를 포함한다.
 */
@Getter
@Setter
@Schema(description = "로그인 요청 DTO")
public class LoginRequestDTO {
	@NotNull(message = "사번은 필수 입력값입니다.")
	@Schema(description = "사번", example = "01234567")
	private Long empId;
	
	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Schema(description = "비밀번호", example = "01234567")
    private String password;
}
