package com.mycrewsoft.domain.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLookupResponse {

    @Schema(description = "사원 ID (프론트 선택값으로 사용)", example = "1001")
    private Long id;

    @Schema(description = "사원명", example = "김철수")
    private String name;

    @Schema(description = "부서명", example = "개발팀")
    private String department;

    @Schema(description = "직책명 우선, 없으면 직급명 (결과 목록 표시용)", example = "팀장")
    private String position;

    @Schema(description = "프로필 사진 URL")
    private String profileImageUrl;
}