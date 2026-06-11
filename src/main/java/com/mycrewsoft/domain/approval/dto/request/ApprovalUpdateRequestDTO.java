package com.mycrewsoft.domain.approval.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결재 템플릿 수정 요청")
public class ApprovalUpdateRequestDTO {

	@Schema(description = "기안서 템플릿 코드", example = "VACATION")
	@NotBlank(message = "템플릿 코드는 필수입니다.")
    private String tmplatCd;
	
    @Schema(description = "템플릿명", example = "휴가 신청서")
    @NotBlank(message = "템플릿명은 필수입니다.")
    @Size(max = 100, message = "템플릿명은 100자 이하이어야 합니다.")
    private String tmplatNm;

    @Schema(description = "템플릿 내용 (HTML)", example = "<form>...</form>")
    @NotBlank(message = "템플릿 내용은 필수입니다.")
    private String tmplatCn;

    @Schema(description = "첨부파일 ID", example = "1001")
    private Long atchFileId;
}
