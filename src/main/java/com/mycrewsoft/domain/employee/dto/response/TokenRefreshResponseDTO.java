package com.mycrewsoft.domain.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 토큰을 재발급 응답에 사용하는 DTO 클래스. 
 * 이 DTO는 백엔드가 토큰을 전달할 때 사용된다.
 */
@Getter
@Builder
@Schema(description = "토큰 재발급 응답 DTO")
public class TokenRefreshResponseDTO {
	
	@Schema(description = "accessToken")
    private String accessToken;
	
	@Schema(description = "refreshToken")
    private String refreshToken;
	
	@Schema(description = "empId")
    private Long empId;
	
	@Schema(description = "authVersion")
    private Integer authVersion;
}