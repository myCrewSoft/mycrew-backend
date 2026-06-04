package com.mycrewsoft.domain.department.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사원 부서 배정 요청")
public class DepartmentMemberAssignRequestDTO {

    @NotEmpty
    @Schema(description = "배정할 사원 ID 목록", example = "[1001, 1002]")
    private List<Long> empIds;
}
