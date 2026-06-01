package com.mycrewsoft.domain.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 요청에 응답을 위한 DTO 클래스. 
 * 로그인 성공 시 발급되는 액세스 토큰(accessToken)과 리프레시 토큰(refreshToken), 
 * 사번(empId), 권한 버전(authVersion), 첫 로그인 여부(firstLoginRequired)를 포함한다.
 */
@Getter
@Setter
@Builder
@Schema(description = "로그인 요청 응답 DTO")
public class LoginResponseDTO {
	private String accessToken;
    private String refreshToken;
    private Long empId;
    private Integer authVersion;
    private boolean firstLoginRequired;
}
