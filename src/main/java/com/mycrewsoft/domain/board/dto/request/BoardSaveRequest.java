package com.mycrewsoft.domain.board.dto.request;

import com.mycrewsoft.validate.groups.UpdateGroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description ="게시글 저장 DTO")
public class BoardSaveRequest {
	
	@NotNull(message = "게시글 번호는 필수입니다.")
	@Schema(description = "게시글 ID", example = "1")
	private Long boardId;
	
	@NotBlank(message ="게시판 유형코드는 필수 입니다." ) 
	@Size(max = 10, message = "게시판 유형 코드는 10자 이하여야 합니다.")
	@Schema(description = "게시판유형코드", example = "NOTICE")
	private String boardTypeCd;
	
	@NotBlank(message = "제목은 필수입니다.")
	@Size(max=256, message ="게시판 제목의 길이 최대는 256자 입니다.")
	@Schema(description = "게시판 제목", example = "2026년 상반기 전사 워크숍 일정 안내")
	private String boardSj;
	
	@NotBlank(message = "내용은 필수입니다.")
    @Size(max = 4000, message = "내용은 4000자 이하여야 합니다.") 
    @Schema(description = "게시판 내용", example = "안녕하세요. 총무팀입니다.")
	private String boardCn;
	
	@Schema(description = "최초 등록자 사원 ID", example = "2")
	private Long frstRgtrId;
	
	@Schema(description = "게시판 첨부파일 ID (첨부파일이 없을 경우 null)", example = "458")
	private Long boardAtchFileId;
	
	@Schema(description = "부서 코드 (부서게시판일 경우 필수)", example = "DEV")
	private String deptCd;
	
	@Schema(description = "프로젝트 ID (프로젝트 관련 게시글일 경우 입력)", example = "15")
	private Long proId;
	
	@NotBlank(message = "중요 공지 여부는 필수입니다.")
    @Size(min = 1, max = 1,message = "중요 공지 여부는 1자여야 합니다.") 
	@Schema(description = "중요 공지 여부 (Y / N)", example = "N", defaultValue = "N")
	private String imprtntYn ="N";
	
	@NotBlank(message = "댓글 허용 여부는 필수입니다.")
    @Size(min = 1, max = 1, message = "댓글 허용 여부는 1자여야 합니다.") 
	@Schema(description = "댓글 허용 여부 (Y / N)", example = "Y", defaultValue = "Y")
	private String cmntUseYn ="Y";

	

}
