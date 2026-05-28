package com.mycrewsoft.domain.file.dto;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "파일 업로드 요청 DTO")
public class FileUploadRequestDto {
	
	@NotNull(message = "파일은 필수입니다.")
    @Schema(description = "업로드할 파일", example = "고양이사진.jpg")
    private MultipartFile file;

    @Schema(description = "파일 세부 설명", example = "팀 회의 자료")
    private String fileCn;

}
