package com.mycrewsoft.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "일정 대상 요청 DTO")
@Getter
@Builder
public class ScheduleTargetResponseDto {

	@Schema(description = "공유 대상 유형 코드 (01=개인, 02=부서)", example = "01")
    private String targetTypeCd;

    @Schema(description = "공유 대상 ID", example = "1001")
    private String targetId;
}
