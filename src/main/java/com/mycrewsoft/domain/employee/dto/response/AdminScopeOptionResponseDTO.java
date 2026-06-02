package com.mycrewsoft.domain.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "관리자 권한 범위 옵션 응답 DTO")
public class AdminScopeOptionResponseDTO {
	
	@Schema(description = "권한 범위 ID", example = "1")
    private String scopeId;
	@Schema(description = "권한 범위 코드", example = "GLOBAL")
    private String scopeCode;
	@Schema(description = "권한 범위 이름", example = "전체 접근")
    private String scopeName;
	@Schema(description = "권한 범위 라벨", example = "DEPT - 부서 접근")
    private String label;

    public AdminScopeOptionResponseDTO(String scopeId, String scopeCode, String scopeName) {
        this.scopeId = scopeId;
        this.scopeCode = scopeCode;
        this.scopeName = scopeName;
        this.label = scopeCode + " - " + scopeName;
    }
}
