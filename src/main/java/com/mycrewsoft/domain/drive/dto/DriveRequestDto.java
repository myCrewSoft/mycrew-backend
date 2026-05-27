package com.mycrewsoft.domain.drive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "드라이브 아이템 생성 요청")
public class DriveRequestDto {
	@Schema(description = "상위드라이브아이템ID (루트면 null)", example = "1")
    private Long prntDriveItemId;

    // 사원ID는 클라이언트가 보내지 않음
    private Long empId;

    @NotBlank(message = "아이템 유형은 필수입니다.")
    @Schema(description = "아이템유형 (01:폴더 / 02:파일)", example = "01")
    private String itemTypeCd;

    @NotBlank(message = "이름은 필수입니다.")
    @Schema(description = "폴더/파일명", example = "프로젝트 자료")
    private String itemNm;

    @Schema(description = "드라이브첨부파일ID", example = "1")
    private Long driveAtchFileId;  // 파일일 때만 사용
}
