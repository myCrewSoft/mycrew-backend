package com.mycrewsoft.domain.file.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "첨부파일 상세 응답 DTO")
public class FileDtlResponseDto {
	@Schema(description = "첨부파일 상세ID", example = "1")
    private Long atchFileDtlId;

    @Schema(description = "원본파일명", example = "고양이사진.jpg")
    private String orgnlFileNm;

    @Schema(description = "파일 타입 코드 (01:IMAGE / 02:DOCUMENT)", example = "01")
    private String atchFileTyCd;

    @Schema(description = "파일 세부 설명", example = "팀 회의 자료")
    private String fileCn;

    @Schema(description = "파일 확장자", example = "jpg")
    private String fileExtsn;

    @Schema(description = "파일 사이즈 (byte)", example = "204800")
    private Long fileSz;

    @Schema(description = "최초등록일시", example = "2024-05-07 14:30:00")
    private String frstRegstDt;   // DateUtil로 포맷된 문자열

    @Schema(description = "등록 후 경과 시간", example = "3분 전")
    private String timeAgo;       // DateUtil.timeAgo()
}
