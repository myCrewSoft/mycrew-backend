package com.mycrewsoft.domain.employee.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "역할 업데이트 요청 DTO")
public class RoleUpdateRequestDTO {

    @NotBlank
    @Schema(description = "역할 코드", example = "ROLE_MANAGER")
    private String roleName;

    @Schema(description = "역할 설명", example = "회사의 관리자 역할입니다.")
    private String description;

    @NotEmpty
    @Schema(description = "역할에 할당할 권한 ID 목록", example = "[1, 2, 3]")
    private List<Long> permissionIds;
}
