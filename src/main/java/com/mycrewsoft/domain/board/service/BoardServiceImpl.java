package com.mycrewsoft.domain.board.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.board.dto.request.BoardCommentCreateRequest;
import com.mycrewsoft.domain.board.dto.request.BoardCommentUpdateRequest;
import com.mycrewsoft.domain.board.dto.request.BoardCreateRequest;
import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.request.BoardUpdateRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.dto.response.BoardSideBarResponse;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.board.vo.BoardCommentVO;
import com.mycrewsoft.domain.board.vo.BoardLikeVo;
import com.mycrewsoft.domain.board.vo.BoardVO;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.PermissionScopeSet;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

	private final EmployeeMapper employeeMapper;
	private final BoardMapper boardMapper;
	private final AuthorizationService authorizationService;
	private final ObjectMapper objectMapper;
	private final DtoMapper dtoMapper;

	// 데이터를 몇 페이지에 몇개씩 보여줄지
	@Override
	@Transactional(readOnly = true)
	public Page<BoardResponse> getBoardList(String boardTypeCd, String deptCd, BoardSearchRequest searchRequest,
			Pageable pageable) {
		if (!StringUtils.isNotBlank(boardTypeCd)) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}

		// 1. 권한 검증 및 자원 설정
		if (StringUtils.isNotBlank(deptCd)) {

			ResourceContext resource = ResourceContext.builder().resourceType(ResourceType.BOARD).deptCd(deptCd)
					.build();

			authorizationService.assertCurrentUserPermission(PermissionCode.BOARD_POST_READ, resource);
		}

		if (StringUtils.isBlank(boardTypeCd)) {
			boardTypeCd = "DEPT";
			searchRequest.setBoardTypeCd(boardTypeCd);
		}

		// 2. 권한 정보(부서코드, 글로벌 여부, 스코프 ID 세트 등) 조회
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		String myDeptCd = employeeMapper.selectEmpDeptCodeByEmpId(currentEmpId);
		PermissionScopeSet scopes = authorizationService.getCurrentPermissionScopes(PermissionCode.BOARD_POST_READ);

		// 3. 데이터베이스 조회 (전체 카운트 및 페이징된 리스트)
		int total = boardMapper.countBoard(searchRequest, boardTypeCd, deptCd);

		// 4. 한 페이지에 보여지는 게시물
		List<BoardResponse> content = boardMapper.getBoardList(pageable.getOffset(), // pageNumber* pageSize
				pageable.getPageSize(), // 한 페이지당 몇개 ?
				searchRequest, // 검색기능
				boardTypeCd, deptCd);
		// 5. Spring Page 객체로 바인딩하여 반환
		return new PageImpl<>(content, pageable, total);
	}

	@Override
	@Transactional
	public List<BoardSideBarResponse> getSideBar() {
		// 1. 권한 검증 및 자원 설정
		ResourceContext resource = ResourceContext.builder().resourceType(ResourceType.BOARD).build();

		authorizationService.assertCurrentUserPermission(PermissionCode.BOARD_POST_READ, resource);

		// 2. 권한 정보(부서코드, 글로벌 여부, 스코프 ID 세트 등) 조회
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		String myDeptCd = employeeMapper.selectEmpDeptCodeByEmpId(currentEmpId);
		PermissionScopeSet scopes = authorizationService.getCurrentPermissionScopes(PermissionCode.BOARD_POST_READ);

		// 3. 데이터베이스 조회 (전체 카운트 및 페이징된 리스트)

		// 게시판 사이드바의 목록 조회
		List<BoardSideBarResponse> rawBoardList = boardMapper.getSideBar(myDeptCd, currentEmpId, scopes.hasGlobal(),
				scopes.getDepartmentScopeIds());
		if (rawBoardList == null) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}

		List<BoardSideBarResponse> resultList = new ArrayList<>(); // 메인 게시판 리스트(공지사항,익명,자유,부서)
		List<BoardSideBarResponse> deptChildren = new ArrayList<>(); // 부서게시판의 하위 게시판

		for (BoardSideBarResponse item : rawBoardList) {
			if ("DEPT".equalsIgnoreCase(item.getBoardTypeCd())) {
				// 💡 boardTypeCd가 'DEPT'인 항목(개발팀, 운영팀 등)은 자식 목록에 차곡차곡 수집합니다.
				deptChildren.add(item);
			} else {
				// 💡 공지사항, 자유게시판, 익명게시판 등 일반 대메뉴는 결과 리스트에 바로 넣습니다.
				item.setUnderlevel(null); // 하위 항목이 없으므로 명시적으로 null 지정
				resultList.add(item);
			}
		}

		BoardSideBarResponse deptParent = BoardSideBarResponse.builder().boardTypeCd("DEPT").boardName("부서게시판")
				// 위에서 열심히 수집한 개발팀, 운영팀 리스트를 underlevel 공간에 주입합니다.
				.underlevel(deptChildren.isEmpty() ? null : deptChildren).build();

		if (!resultList.isEmpty()) {
			resultList.add(1, deptParent);
		} else {
			resultList.add(deptParent);
		}

		return resultList;
	}

	@Override
	@Transactional
	public BoardResponse getBoard(String deptCd, Long boardId) {

		Long empId = SecurityUtil.getCurrentEmpId();
		// 1. 권한 검증 및 자원 설정
		// 부서코드가 비어있지않으면
		if (StringUtils.isNotBlank(deptCd)) {
			ResourceContext resource = ResourceContext.builder().resourceType(ResourceType.BOARD).deptCd(deptCd)
					.build();

			authorizationService.assertCurrentUserPermission(PermissionCode.BOARD_POST_READ, resource);
		}
		// 2. 데이터베이스 조회 (게시물의 게시글, 게시판 댓글, 좋아요,조회수,좋아요 수 )
		int updatedView = boardMapper.updateViewCount(boardId);
		if (updatedView == 0) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}

		BoardVO boardVo = boardMapper.readBoard(boardId);
		if (boardVo == null) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}

		List<BoardCommentVO> boardCommentVO = boardMapper.readCommentList(boardId);

		BoardLikeVo boardLikeVo = boardMapper.readLikeStatus(boardId, empId);

		int boardLikeCount = boardMapper.readLikeCount(boardId);

		// vo 를 dto로 바꾸는 작업
		BoardResponse boardResponse = dtoMapper.toDto(boardVo, BoardResponse.class);

		boardResponse.setCommentList(dtoMapper.toDtoList(boardCommentVO, BoardCommentVO.class));
		boardResponse.setIsLiked(boardLikeVo != null);

		boardResponse.setLikeCnt(boardLikeCount);

		try {
			log.info("BoardResponse Data: {}", objectMapper.writeValueAsString(boardResponse));
		} catch (Exception e) {
			log.warn("BoardResponse 로그 변환 실패: {}", e.getMessage());
		}
		return boardResponse;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<BoardResponse> getProjList(Long projId, BoardSearchRequest searchRequest, Pageable pageable) {

		// 프로젝트가 없으면 프로젝트가 없다는 예외
		if (projId == null) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}

		Long currentEmpId = SecurityUtil.getCurrentEmpId();

		// 프로젝트 하는 이들만 볼 수있는 권한 체크 -> 지금 목록을 보려고 하는 사람이 프로젝트 참여자인지
		// currentEmpId == mapper.getempId(projId);

		// 1. 해당 프로젝트 게시글의 전체 카운트 조회
		int total = boardMapper.countProjBoard(searchRequest, projId);

		// 2. 한 페이지에 보여지는 프로젝트 게시물 리스트 조회 (getBoardList와 동일 포맷)
		List<BoardResponse> content = boardMapper.getProjList(pageable.getOffset(), pageable.getPageSize(),
				searchRequest, projId);

		// 3. Spring Page 객체로 바인딩하여 최종 반환
		return new PageImpl<>(content, pageable, total);
	}

	// 게시글 생성 메서드
	@Override
	@Transactional
	public Long createBoard(BoardCreateRequest boardCreateRequest) {

		// 권한 체크
		if ("notice".equals(boardCreateRequest.getBoardTypeCd())) {
			// 공지사항 일때는 관리자만

		} else if ("dept".equals(boardCreateRequest.getBoardTypeCd())) {
			// 부서게시판은 소속된 부서사람들만

		} else if ("proj".equals(boardCreateRequest.getBoardTypeCd())) {
			// 프로젝트 게시판은 프로젝트하는 사람들만
		} else {
			// 자유와 익명은 직원들 전체 아무나
		}
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// DTO -> VO로 변환
		BoardVO boardVo = dtoMapper.toDto(boardCreateRequest, BoardVO.class);

		boardVo.setFrstRgtrId(empId);
		// 데이터베이스에서 생성
		boardMapper.createBoard(boardVo);

		return boardVo.getBoardId();
	}

	@Override
	@Transactional
	public Long updateBoardDetail(Long boardId, BoardUpdateRequest boardUpdateRequest) {
		// 로그인한 사원 아이디 empId
		Long empId = SecurityUtil.getCurrentEmpId();
		
		
		// db에서 글을 조회
		BoardVO writtenBoard  = boardMapper.readBoard(boardId);
		if (writtenBoard == null) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}

		Long writer	= writtenBoard.getFrstRgtrId();
		
		//내가 작성한 글만 권한
		// 현재 접속한 사원 아이디 = 작성한 사람 아이디
		if(!empId.equals(writer)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
		
		//DTO-VO로 변환
		BoardVO boardDetail =dtoMapper.toDto(boardUpdateRequest,  BoardVO.class);
		boardDetail.setBoardId(boardId);
		
		//데이터베이스에서 생성
		int result= boardMapper.updateBoardDetails(boardDetail);
		if(result==0) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}
		return boardId; // 게시물 수정하면 그 게시물로 이동하기 때문에 
	}
	
	@Override
	@Transactional
	public void deleteBoardDetail(Long boardId) {
		// 로그인한 사원 아이디 empId
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// db에서 글을 조회
		BoardVO readedBoard  = boardMapper.readBoard(boardId);
		if (readedBoard == null) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}

		Long written = readedBoard.getFrstRgtrId();
		
		//내가 작성한 글만 권한
		// 현재 접속한 사원 아이디 = 작성한 사람 아이디
		if(!empId.equals(written)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		//데이터베이스에서 논리 삭제
		int result = boardMapper.deleteBoardDetail(boardId);
		if(result ==0) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}
		
		
	}

	@Transactional
	@Override
	public Long createComment(BoardCommentCreateRequest createComment) {
		
		// 권한 체크
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// DTO -> VO로 변환
		BoardCommentVO boardCommentVO = dtoMapper.toDto(createComment, BoardCommentVO.class);

		boardCommentVO.setWrterEmpId(empId);
	
		// 데이터베이스에서 생성
		boardMapper.insertComment(boardCommentVO);

		return boardCommentVO.getCommentId();
	}

	@Override
	@Transactional
	public Long updateComment(BoardCommentUpdateRequest updateComment) {

		// 권한 체크
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// DTO -> VO로 변환
		BoardCommentVO updateVo = dtoMapper.toDto(updateComment , BoardCommentVO.class);
		
		updateVo.setWrterEmpId(empId);
		
				
		//데이터베이스에서 생성
		int result= boardMapper.updateComment(updateVo);
		
		if(result==0) {
		throw new CustomException(ErrorCode.ACCESS_DENIED);
			}
		return updateVo.getCommentId(); 
			
	}

	@Override
	@Transactional
	public void deleteComment(Long commentId) {
		
		// 권한 체크
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// db에서 글을 조회
		 Long readCommentEmpId = boardMapper.readCmWrterEmpId(commentId); 
		
		 //댓글 못 찾음
		if(readCommentEmpId == null) {
			throw new CustomException(ErrorCode.COMMENT_NOT_FOUND); 
		}
		
		// 로그인한 사람과 게시판 댓글 작성자 아이디  동일하지않으면 
		if(!empId.equals(readCommentEmpId)){
			throw new CustomException(ErrorCode.ACCESS_DENIED);

		}
		//데이터 베이스에서 논리삭제 
		int result =	boardMapper.deleteComment(commentId);
		if(result ==0) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
	}

	@Override
	public boolean toggelLike(Long boardId, Long empId) {
	
		// DB에서 이 글에 이 사람이 좋아요를 누른 데이터가 있는지 조회
		BoardLikeVo likeStatus = boardMapper.readLikeStatus(boardId, empId);
		// 만약 결과가 NULL 이라면? (즉, 하트를 처음 누르는 상황)
		if(likeStatus ==null) {
			// 글 번호와 직원사번 를 채워 넣음
			BoardLikeVo newLike = new BoardLikeVo();
			newLike.setBoardId(boardId);
			newLike.setEmpId(empId);
			
			//DB 에 이사람 이 글 좋아요 눌렀음 하고 저장함
			boardMapper.insertLike(newLike);
			return true;
		}else {
			//DB 에 이제 좋아요가 취소되었습니다 false 를 리턴함
			boardMapper.deleteLike(boardId, empId);
			return false;
		}
		
		
	}

	@Override
	public Map<String, Object> getLike(Long boardId, Long empId) {
	
		// map 을 사용하는 이유는 타입이 다른 int 와 boolean을 사용하기 때문에  
		Map<String, Object> result = new HashMap<>();
		
		int likeCount = boardMapper.readLikeCount(boardId);
		
		boolean isLiked = boardMapper.readLikeStatus(boardId, empId)!=null;
		
		result.put("likeCount", likeCount);
		result.put("isLiked", isLiked);
		return result;
	}

}
