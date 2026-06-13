package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 마이페이지 개인정보(이름/휴대전화/주소) 변경 요청 DTO.
 */
@Getter
@Setter
@Schema(description = "개인정보(이름/휴대전화/주소) 변경 요청 DTO")
public class ChangeProfileInfoRequestDTO {

	@Schema(description = "이름", example = "임원호")
	@NotBlank(message = "이름은 필수입니다.")
	@Size(max = 50, message = "이름은 50자 이하이어야 합니다.")
	private String empNm;

	@Schema(description = "휴대전화 번호", example = "010-1234-5678")
	@Pattern(
			regexp = "^$|^01[016789]-?\\d{3,4}-?\\d{4}$",
			message = "휴대전화 번호 형식이 올바르지 않습니다.")
	private String mblTelno;

	@Schema(description = "우편번호", example = "06234")
	@Size(max = 10, message = "우편번호는 10자 이하이어야 합니다.")
	private String zip;

	@Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
	@Size(max = 300, message = "주소는 300자 이하이어야 합니다.")
	private String addr;
}
