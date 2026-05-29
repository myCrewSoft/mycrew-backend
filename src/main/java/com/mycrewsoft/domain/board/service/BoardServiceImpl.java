package com.mycrewsoft.domain.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.board.vo.BoardVO;
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
    
    /**
     * 💡 하나로 합친 통합 게시글 목록 조회 비즈니스 로직
     * @param searchRequest 검색 조건(유형, 부서, 검색어)이 담긴 DTO
     * @return 필터링된 게시글 리스트
     */
    @Override
    @Transactional
    public List<BoardVO> selectBoardList(BoardSearchRequest searchRequest) {
		 authorizationService.assertCurrentUserPermission(
		 PermissionCode.BOARD_POST_READ, ResourceContext.builder()
		 .resourceType(ResourceType.BOARD) .build() );
		 
		 Long currentEmpId = SecurityUtil.getCurrentEmpId();
		 
		 String myDeptCd = employeeMapper.selectEmpDeptCodeByEmpId(currentEmpId);
		 
		 PermissionScopeSet scopes =
		 authorizationService.getCurrentPermissionScopes(PermissionCode.
		 BOARD_POST_READ);
		 
		 return boardMapper.selectBoardList( searchRequest, currentEmpId, myDeptCd,
		 scopes.hasGlobal(), scopes.getDepartmentScopeIds(),
		 scopes.getProjectScopeIds() );
    }
    
    
    
    
}