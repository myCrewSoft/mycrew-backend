package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "권한 상태 변경 요청 DTO")
public class PermissionStatusUpdateRequestDTO {

    @NotBlank
    @Schema(description = "변경할 권한 상태", example = "Y")
    private String enabled;
}
