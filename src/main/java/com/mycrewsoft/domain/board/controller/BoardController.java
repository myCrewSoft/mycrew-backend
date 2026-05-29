package com.mycrewsoft.domain.board.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.service.BoardService;
import com.mycrewsoft.domain.board.vo.BoardVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *  게시판 컨트롤러
 *  requestMapping 을 통해 url 지정
 */
@Slf4j
@Tag(name = "Board", description = "게시판 API")
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
	
	private  final BoardService service;


	/*
	 * @Operation(summary = "게시판 목록 조회")
	 * 
	 * @GetMapping public ResponseEntity<ApiResponse<List<BoardVO>>>
	 * BoardTypeList(){
	 * 
	 * // 게시판 가져옴 List<BoardVO> boardList = service.selectNoticeList();
	 * 
	 * return ResponseEntity.ok(ApiResponse.success(boardList)); }
	 */
	
	@Operation(summary = "게시글 목록 조회")
	@GetMapping
	public ResponseEntity<ApiResponse<List<BoardVO>>> BoardList(
			@Validated BoardSearchRequest searchRequest 
	){
	
		List<BoardVO> boardList = service.selectBoardList(searchRequest);
		
		return ResponseEntity.ok(ApiResponse.success("게시판 목록 불러오기 성공", boardList));
	}
	
}
