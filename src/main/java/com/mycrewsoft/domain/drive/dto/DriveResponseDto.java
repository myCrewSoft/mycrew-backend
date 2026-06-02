package com.mycrewsoft.domain.drive.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "드라이브 응답 DTO")
public class DriveResponseDto {
	@Schema(description = "드라이브아이템ID", example = "1")
    private Long driveItemId;

    @Schema(description = "상위드라이브아이템ID", example = "1")
    private Long prntDriveItemId;

    @Schema(description = "아이템유형 (01:폴더 / 02:파일)", example = "01")
    private String itemTypeCd;

    @Schema(description = "즐겨찾기여부", example = "Y")
    private String bookmarkYn;

    @Schema(description = "폴더/파일명", example = "프로젝트 자료")
    private String itemNm;

    @Schema(description = "최초등록일시", example = "2024-05-07 14:30:00")
    private String frstRegDt;

    @Schema(description = "최종수정일시", example = "2024-05-07 14:30:00")
    private String lastMdfcnDt;

    @Schema(description = "등록 후 경과 시간", example = "3분 전")
    private String timeAgo;
    
    private String fileSz;
    private String orgnFileNm;
}
