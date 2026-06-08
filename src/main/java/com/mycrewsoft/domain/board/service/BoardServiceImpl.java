package com.mycrewsoft.domain.board.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.mycrewsoft.domain.board.controller.BoardController;
import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
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
	
	@Autowired
	private javax.sql.DataSource dataSource;
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

		if(StringUtils.isBlank(boardTypeCd)) {
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
		List<BoardResponse> content = boardMapper.getProjList(
				pageable.getOffset(), 
				pageable.getPageSize(), 
				searchRequest, 
				projId
		);
		
		// 3. Spring Page 객체로 바인딩하여 최종 반환
		return new PageImpl<>(content, pageable, total);
	}


}