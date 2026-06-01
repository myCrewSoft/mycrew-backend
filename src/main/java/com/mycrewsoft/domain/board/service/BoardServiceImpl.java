package com.mycrewsoft.domain.board.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.board.dto.response.BoardSideBarResponse;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.PermissionCode;
import com.mycrewsoft.security.authz.PermissionScopeSet;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
	private final EmployeeMapper employeeMapper;
	private final BoardMapper boardMapper;  
	private final AuthorizationService authorizationService;

//	@Override
//	@Transactional(readOnly = true)
//	public Page<BoardResponse> getBoard(BoardSearchRequest condition) {
//		// 1. 권한 검증 및 자원 설정
//		ResourceContext resource = ResourceContext.builder()
//				.resourceType(ResourceType.BOARD)
//				.build();
//
//		authorizationService.assertCurrentUserPermission(
//				PermissionCode.BOARD_POST_READ,
//				resource
//				);
//
//		// 2. 권한 정보(부서코드, 글로벌 여부, 스코프 ID 세트 등) 조회
//		Long currentEmpId = SecurityUtil.getCurrentEmpId();
//		String myDeptCd = employeeMapper.selectEmpDeptCodeByEmpId(currentEmpId);
//		PermissionScopeSet scopes = authorizationService.getCurrentPermissionScopes(PermissionCode.BOARD_POST_READ);
//
//		// 3. 페이징 계산
//		int page = Math.max(condition.getPage(), 0);
//		int size = Math.min(Math.max(condition.getSize(), 1), 100);
//		int offset = page * size;
//
//		// 4. 데이터베이스 조회 (전체 카운트 및 페이징된 리스트)
//		long total = boardMapper.countBoard(condition);
//
//		List<BoardResponse> content = boardMapper.selectBoard(
//				condition, 
//				currentEmpId, 
//				myDeptCd,
//				scopes.hasGlobal(), 
//				scopes.getDepartmentScopeIds(),
//				scopes.getProjectScopeIds(),
//				offset, 
//				size
//				);
//
//		// 5. Spring Page 객체로 바인딩하여 반환
//		Pageable pageable = PageRequest.of(page, size);
//		return new PageImpl<>(content, pageable, total);
//	}

	@Override
	public List<BoardSideBarResponse> getSideBar() {
		// 1. 권한 검증 및 자원 설정
		ResourceContext resource = ResourceContext.builder()
				.resourceType(ResourceType.BOARD)
				.build();

		authorizationService.assertCurrentUserPermission(
				PermissionCode.BOARD_POST_READ,
				resource
				);
		
		// 2. 권한 정보(부서코드, 글로벌 여부, 스코프 ID 세트 등) 조회
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		String myDeptCd = employeeMapper.selectEmpDeptCodeByEmpId(currentEmpId);
		PermissionScopeSet scopes = 
				authorizationService.getCurrentPermissionScopes(PermissionCode.BOARD_POST_READ);

		// 3. 데이터베이스 조회 (전체 카운트 및 페이징된 리스트)
	
		
		//게시판 사이드바의 목록 조회
		List<BoardSideBarResponse> rawBoardList = boardMapper.getSideBar(myDeptCd,currentEmpId,scopes.hasGlobal(),scopes.getDepartmentScopeIds());
		if(rawBoardList ==null) {
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
		
		BoardSideBarResponse deptParent = BoardSideBarResponse.builder()
	            .boardTypeCd("DEPT")
	            .boardName("부서게시판")
	            // 위에서 열심히 수집한 개발팀, 운영팀 리스트를 underlevel 공간에 주입합니다.
	            .underlevel(deptChildren.isEmpty() ? null : deptChildren)
	            .build();
		
		
		if (!resultList.isEmpty()) {
	        resultList.add(1, deptParent);
	    } else {
	        resultList.add(deptParent);
	    }
	    
	    return resultList;
	}
}