package com.mycrewsoft.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "게시글 목록 검색 요청 정보")
public class BoardSearchRequest {

	@Size(max=256 ,message ="검색어는 256자 이하여야 합니다.")
	@Schema(description = "검색어(게시글 제목 기준 검색)",example = "워크숍") 
	private String keyword; //검색어
	
	@Size(max=10, message = "게시판 유형 코드는 10자 이하여야 합니다.")
	@Schema(description = "게시판 유형코드",example = "FREE")
	private String boardTypeCd; //게시판유형

    @Schema(description = "부서 코드 (부서게시판 조회 시 사용)", example = "DEV")
	private String deptCd; //부서코드
}
