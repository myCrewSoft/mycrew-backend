package com.mycrewsoft.domain.board.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.dto.response.BoardSideBarResponse;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.PermissionScopeSet;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
public class BoardServiceImplTest {

    @Mock private EmployeeMapper employeeMapper;
    @Mock private BoardMapper boardMapper;
    @Mock private AuthorizationService authorizationService;

    @InjectMocks private BoardServiceImpl boardService;

    @Test
    @DisplayName("게시글 목록 조회 성공 테스트")
    void getBoardList_Success() {
        // 1. MockedStatic을 사용하여 static 메서드 가로채기
        try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
            // SecurityUtil.getCurrentEmpId() 호출 시 1L을 반환하도록 설정
            mockedSecurity.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

            // Given (준비)
            String boardTypeCd = "FREE";
            String deptCd = null;
            BoardSearchRequest searchRequest = new BoardSearchRequest();
            Pageable pageable = PageRequest.of(0, 10);

            // ... 나머지 Stubbing 설정 ...
            when(boardMapper.countBoard(any(), eq(boardTypeCd), eq(deptCd))).thenReturn(100L);
            when(boardMapper.getBoardList(anyLong(), anyInt(), any(), eq(boardTypeCd), eq(deptCd)))
                .thenReturn(List.of(new BoardResponse()));

            // 2. When (실행)
            Page<BoardResponse> result = boardService.getBoard(boardTypeCd, deptCd, searchRequest, pageable);

            // 3. Then (검증)
            assertNotNull(result);
        }
    }
    @Test
    @DisplayName("사이드바 메뉴 구성 성공 테스트")
    void getSideBar_Success() {
        // 1. Static 메서드 가로채기 (SecurityUtil)
        try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

            // 2. Mock 객체 설정 (authorizationService 등)
            PermissionScopeSet mockScopes = mock(PermissionScopeSet.class);
            when(mockScopes.hasGlobal()).thenReturn(true);
            when(authorizationService.getCurrentPermissionScopes(any())).thenReturn(mockScopes);
            
            // 부서코드 조회 모킹
            when(employeeMapper.selectEmpDeptCodeByEmpId(anyLong())).thenReturn("DEPT01");

            // DB 조회 결과 모킹 (rawBoardList 반환)
            BoardSideBarResponse item = new BoardSideBarResponse();
            item.setBoardTypeCd("FREE");
            when(boardMapper.getSideBar(any(), anyLong(), anyBoolean(), any())).thenReturn(List.of(item));

            // 3. 실행
            List<BoardSideBarResponse> result = boardService.getSideBar();

            // 4. 검증
            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(authorizationService, times(1)).assertCurrentUserPermission(any(), any());
        }
    }
}