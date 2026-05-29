package com.mycrewsoft.domain.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "일정 대상 요청 DTO")
@Getter
@NoArgsConstructor
public class ScheduleTargetRequestDto {

	@Schema(description = "공유 대상 유형 코드 (01=개인, 02=부서)", example = "01")
    @NotBlank(message = "공유 대상 유형 코드는 필수입니다.")
    private String targetTypeCd;

    @Schema(description = "공유 대상 ID (사번 또는 부서코드)", example = "1001")
    @NotBlank(message = "공유 대상 ID는 필수입니다.")
    private String targetId;
}
