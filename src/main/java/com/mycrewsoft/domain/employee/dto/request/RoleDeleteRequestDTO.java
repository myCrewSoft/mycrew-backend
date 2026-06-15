package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "역할 삭제 요청 DTO")
public class RoleDeleteRequestDTO {
	@Schema(description = "삭제할 역할 ID", example = "1")
    private Long replacementRoleId;
}
