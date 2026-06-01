package com.mycrewsoft.domain.file.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "첨부파일 분류 응답 DTO")
public class FileClsfResponseDto {
	@Schema(description = "첨부파일ID", example = "1")
    private Long atchFileId;

    @Schema(description = "사용여부", example = "Y")
    private String useYn;

    @Schema(description = "파일 목록")
    private List<FileDtlResponseDto> fileDtlList;  // HAS 관계 반영
}
