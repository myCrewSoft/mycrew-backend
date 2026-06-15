package com.mycrewsoft.domain.board.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page; // 💡 Page 임포트 추가
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // 💡 @ModelAttribute 명시 권장
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.board.dto.request.BoardCommentCreateRequest;
import com.mycrewsoft.domain.board.dto.request.BoardCommentUpdateRequest;
import com.mycrewsoft.domain.board.dto.request.BoardCreateRequest;
import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.request.BoardUpdateRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.dto.response.BoardSideBarResponse;
import com.mycrewsoft.domain.board.service.BoardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
	public ResponseEntity<ApiResponse<Page<BoardResponse>>> getBoardList(@PathVariable String boardTypeCd,
			@Validated @ModelAttribute BoardSearchRequest searchRequest,
			@PageableDefault(size = 10, sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable) {
		// 1. 서비스의 페이징 메서드(getBoard)를 호출하고 Page 객체로 받습니다.
		Page<BoardResponse> boardPage = service.getBoardList(boardTypeCd, null, searchRequest, pageable);

		// 2. 응답 데이터 타입을 Page<BoardResponse>로 일치시켜 전송합니다. return
		return ResponseEntity.ok(ApiResponse.success("게시판 목록 불러오기 성공!", boardPage));
	}

	@Operation(summary = "부서게시글 목록 조회 (페이징 적용)")
	@GetMapping("/DEPT/{deptCd}")
	public ResponseEntity<ApiResponse<Page<BoardResponse>>> getDeptList(
			@PathVariable String deptCd,
			@Validated @ModelAttribute BoardSearchRequest searchRequest,
			@PageableDefault(size = 10, sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable) {
		// 1. 서비스의 페이징 메서드(getBoard)를 호출하고 Page 객체로 받습니다.
		Page<BoardResponse> boardPage = service.getBoardList("DEPT", deptCd, searchRequest, pageable);

		// 2. 응답 데이터 타입을 Page<BoardResponse>로 일치시켜 전송합니다. return
		return ResponseEntity.ok(ApiResponse.success("부서 게시판 목록 불러오기 성공!", boardPage));
	}

	@Operation(summary = "프로젝트 게시글 목록 조회 (페이징 적용)")
	@GetMapping("/PROJ/{projId}")
	public ResponseEntity<Page<BoardResponse>> getProjList(
			@PathVariable("projId") Long projId,
			@ModelAttribute BoardSearchRequest searchRequest,
			@PageableDefault(size = 10,sort ="boardId",direction = Sort.Direction.DESC) Pageable pageable) {

		// 서비스 레이어 호출하여 Page 결과 확보
		Page<BoardResponse> projectBoards = service.getProjList(projId, searchRequest, pageable);

		// 프로젝트 공통 응답 객체 포맷이 있다면 래핑하셔도 좋습니다. (예: ApiResponse.success(projectBoards))
		return ResponseEntity.ok(projectBoards);
	}

	@Operation(summary = "게시판 사이드바 조회")
	@GetMapping("/sidebar")
	public ResponseEntity<ApiResponse<List<BoardSideBarResponse>>> getSideBar() {

		List<BoardSideBarResponse> sideBar = service.getSideBar();

		return ResponseEntity.ok(ApiResponse.success(sideBar));
	}


	@Operation(summary = "일반 게시판 게시글 조회")
	@GetMapping("/{boardTypeCd}/{boardId:\\d+}")
	public ResponseEntity<ApiResponse<BoardResponse>> getBoard(
			@PathVariable String boardTypeCd,
			@PathVariable Long boardId) {

		BoardResponse boardResponse = service.getBoard(null, boardId);

		return ResponseEntity.ok(ApiResponse.success("일반 게시글 상세 조회 성공!", boardResponse));
	}


	@Operation(summary = "부서 게시판 게시글 조회")
	@GetMapping("/DEPT/{deptCd}/{boardId:\\d+}")
	public ResponseEntity<ApiResponse<BoardResponse>> getDept(
			@PathVariable	String deptCd,
			@PathVariable	Long boardId) {

		BoardResponse boardResponse = service.getBoard(deptCd, boardId);

		return ResponseEntity.ok(ApiResponse.success("부서 게시글 상세 조회 성공!", boardResponse));
	}


	@Operation(summary = "게시글 작성")
	@PostMapping
	public ResponseEntity<ApiResponse<Long>> wirteBoard(
			@Valid	@RequestBody  BoardCreateRequest boardCreateRequest
			) {

		Long boardId = service.createBoard(boardCreateRequest);

		return ResponseEntity.ok(ApiResponse.success("게시글 생성 성공!", boardId));
	}


	@Operation(summary = "게시글 수정")
	@PutMapping("/{boardId}")
	public ResponseEntity<ApiResponse<Long>> updateBoardDetail(
			@PathVariable Long boardId, 
			@Valid @RequestBody BoardUpdateRequest boardUpdateDetail
			) {


		Long updatedBoardId = service.updateBoardDetail(boardId,boardUpdateDetail);

		return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공!", updatedBoardId));
	}

	@Operation(summary = "게시글 삭제")
	@DeleteMapping("/{boardId}")
	public ResponseEntity<ApiResponse<String>> deleteBoardDetail(
			@PathVariable Long boardId
			) {


		 service.deleteBoardDetail(boardId);

		return ResponseEntity.ok(ApiResponse.success("게시글 삭제 성공!"));
	}
	
	
	@Operation(summary = "게시글 댓글 작성")
	@PostMapping("/comments")
	public ResponseEntity<ApiResponse<Long>> createComment(
			@Valid	@RequestBody  BoardCommentCreateRequest boardCommentRequest
			) {

		Long commentId = service.createComment(boardCommentRequest);

		return ResponseEntity.ok(ApiResponse.success("게시글 댓글 등록 성공!", commentId));
	}
	
	

	@Operation(summary = "게시글 댓글 수정")
	@PutMapping("/comments")
	public ResponseEntity<ApiResponse<Long>> updateComment(
			@Valid @RequestBody BoardCommentUpdateRequest boardUpdateComment
			) {
		Long commentId = service.updateComment(boardUpdateComment);

		return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공!", commentId));
	}
	
	
	
	
	@Operation(summary = "게시글 댓글 삭제")
	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<ApiResponse<String>> deleteComment(
			@PathVariable("commentId") Long commentId
			
			) {


		 service.deleteComment(commentId);

		return ResponseEntity.ok(ApiResponse.success("게시글 댓글 삭제 성공!"));
	}
	
	@Operation(summary = "게시글 좋아요 상태 및 개수 조회")
	@GetMapping("/{boardId}/like")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getLike(
			@PathVariable("boardId") Long boardId,
			@RequestParam("empId") Long empId){
		
		Map<String, Object> likeInfo =	service.getLike(boardId, empId);
		
		return ResponseEntity.ok(ApiResponse.success("좋아요 정보 조회 성공!", likeInfo));
			}
		
	@Operation(summary = "게시글 좋아요 토글(등록/취소)")
	@PostMapping("/{boardId}/like")
	public ResponseEntity<ApiResponse<Boolean>> toggleLike(
			@PathVariable("boardId") Long boardId,
			@RequestParam("empId") Long empId){
		boolean isLiked =service.toggelLike(boardId, empId);
		
		String message =isLiked ? "게시글 좋아요 등록 성공!" :"게시글 좋아요 취소 성공";
		return ResponseEntity.ok(ApiResponse.success(message,isLiked));	
	}
	
}