package com.mycrewsoft.domain.board.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
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
    
    @Override
    @Transactional(readOnly = true)
    public Page<BoardResponse> getBoard(BoardSearchRequest condition) {
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
        PermissionScopeSet scopes = authorizationService.getCurrentPermissionScopes(PermissionCode.BOARD_POST_READ);

        // 3. 페이징 계산
        int page = Math.max(condition.getPage(), 0);
        int size = Math.min(Math.max(condition.getSize(), 1), 100);
        int offset = page * size;

        // 4. 데이터베이스 조회 (전체 카운트 및 페이징된 리스트)
        long total = boardMapper.countBoard(condition);

        List<BoardResponse> content = boardMapper.selectBoard(
                condition, 
                currentEmpId, 
                myDeptCd,
                scopes.hasGlobal(), 
                scopes.getDepartmentScopeIds(),
                scopes.getProjectScopeIds(),
                offset, 
                size
        );

        // 5. Spring Page 객체로 바인딩하여 반환
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, total);
    }
}