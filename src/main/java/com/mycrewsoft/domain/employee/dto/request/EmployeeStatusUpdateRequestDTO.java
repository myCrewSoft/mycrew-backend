package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자의 사원 상태 변경 요청 DTO.
 */
@Getter
@Setter
@Schema(description = "사원 상태 변경 요청 DTO")
public class EmployeeStatusUpdateRequestDTO {

    @NotBlank(message = "직원 상태 코드는 필수 입력값입니다.")
    @Schema(description = "변경할 직원 상태 코드", example = "EMP_INACTIVE")
    private String empStatCd;
}
