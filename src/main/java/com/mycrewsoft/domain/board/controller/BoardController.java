package com.mycrewsoft.domain.board.controller;

import java.util.List;

import org.springframework.data.domain.Page; // 💡 Page 임포트 추가
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // 💡 @ModelAttribute 명시 권장
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

	/*
	 * @Operation(summary = "게시글 목록 조회 (페이징 적용)")
	 * 
	 * @GetMapping public ResponseEntity<ApiResponse<Page<BoardResponse>>> getRes(
	 * 
	 * @Validated @ModelAttribute BoardSearchRequest searchRequest ){ // 1. 서비스의 페이징
	 * 메서드(getBoard)를 호출하고 Page 객체로 받습니다. Page<BoardResponse> boardPage =
	 * service.getBoard(searchRequest);
	 * 
	 * // 2. 응답 데이터 타입을 Page<BoardResponse>로 일치시켜 전송합니다. return
	 * ResponseEntity.ok(ApiResponse.success("게시판 목록 불러오기 성공!", boardPage)); }
	 */
	
	@Operation(summary = "게시판 사이드바 조회")
	@GetMapping("/sidebar")
	public ResponseEntity<ApiResponse<List<BoardSideBarResponse>>> getSideBar(
			
	){
	
		List<BoardSideBarResponse> sideBar = service.getSideBar(); 
		
		return ResponseEntity.ok(ApiResponse.success(sideBar));
	}
	
	
}