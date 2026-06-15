package com.mycrewsoft.domain.board.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시판 사이드바")
public class BoardSideBarResponse {

	@Schema(description = "게시판 유형코드", example = "FREE")
	private String boardTypeCd;

	@Schema(description = "게시판 이름", example = "자유게시판")
	private String boardName;

	@Schema(description = "부서 코드 (부서게시판 하위 항목)", example = "DEV")
	private String deptCd;
	
	@Schema(description = "하위 부서/팀 게시판 목록", example = "null 또는 하위 리스트 객체")
	private List<BoardSideBarResponse> underlevel;
	
}
