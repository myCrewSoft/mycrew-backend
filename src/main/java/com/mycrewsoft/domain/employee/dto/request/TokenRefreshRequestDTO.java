package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 토큰을 재발급 해달라는 요청이 발생했을 때 사용하는 DTO 클래스. 
 * 클라이언트가 서버에 리프레시 토큰을 보내면, 
 * 서버는 해당 토큰이 유효한지 검증한 후 새로운 액세스 토큰과 리프레시 토큰을 발급한다. 
 * 이 DTO는 클라이언트가 리프레시 토큰을 전달할 때 사용된다.
 */
@Getter
@Setter
@Schema(description = "토큰 재발급 요청 DTO")
public class TokenRefreshRequestDTO {

    @NotBlank(message = "Refresh Token은 필수입니다.")
    @Schema(description = "Refresh Token")
    private String refreshToken;
}
