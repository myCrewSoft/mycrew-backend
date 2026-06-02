package com.mycrewsoft.domain.board.controller;

import java.util.List;

import org.springframework.data.domain.Page; // 💡 Page 임포트 추가
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // 💡 @ModelAttribute 명시 권장
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.dto.response.BoardSideBarResponse;
import com.mycrewsoft.domain.board.service.BoardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 게시판 컨트롤러
 */
@Slf4j
@Tag(name = "Board", description = "게시판 API")
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

	private final BoardService service;

	@Operation(summary = "일반 게시글 목록 조회 (페이징 적용)-자유,익명,공지")
	@GetMapping("/{boardTypeCd}")
	public ResponseEntity<ApiResponse<Page<BoardResponse>>> getBoardList(
			@PathVariable String boardTypeCd,
			@Validated @ModelAttribute BoardSearchRequest searchRequest,
			@PageableDefault(
					size =10,
					sort = "boardId",
					direction = Sort.Direction.DESC
					)
			Pageable pageable
			) {
		// 1. 서비스의 페이징 메서드(getBoard)를 호출하고 Page 객체로 받습니다.
		Page<BoardResponse> boardPage = service.getBoard(boardTypeCd,null,searchRequest,pageable);
		
		// 2. 응답 데이터 타입을 Page<BoardResponse>로 일치시켜 전송합니다. return
		return ResponseEntity.ok(ApiResponse.success("게시판 목록 불러오기 성공!", boardPage));
	}
	
	@Operation(summary = "부서게시글 목록 조회 (페이징 적용)")
	@GetMapping("/dept/{deptCd}")
	public ResponseEntity<ApiResponse<Page<BoardResponse>>> getDeptList(
			@PathVariable String deptCd,
			@Validated @ModelAttribute BoardSearchRequest searchRequest,
			@PageableDefault(
					size =10,
					sort = "boardId",
					direction = Sort.Direction.DESC
					)
			Pageable pageable
			) {
		// 1. 서비스의 페이징 메서드(getBoard)를 호출하고 Page 객체로 받습니다.
		Page<BoardResponse> boardPage = service.getBoard("dept",deptCd,searchRequest,pageable);
		
		// 2. 응답 데이터 타입을 Page<BoardResponse>로 일치시켜 전송합니다. return
		return ResponseEntity.ok(ApiResponse.success("게시판 목록 불러오기 성공!", boardPage));
	}

	@Operation(summary = "게시판 사이드바 조회")
	@GetMapping("/sidebar")
	public ResponseEntity<ApiResponse<List<BoardSideBarResponse>>> getSideBar() {

		List<BoardSideBarResponse> sideBar = service.getSideBar();

		return ResponseEntity.ok(ApiResponse.success(sideBar));
	}

}