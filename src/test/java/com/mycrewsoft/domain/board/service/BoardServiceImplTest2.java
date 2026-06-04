package com.mycrewsoft.domain.board.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.board.vo.BoardCommentVO;
import com.mycrewsoft.domain.board.vo.BoardLikeVo;
import com.mycrewsoft.domain.board.vo.BoardVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest2 {

    @InjectMocks
    private BoardServiceImpl boardService;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private DtoMapper dtoMapper;

    @Test
    @DisplayName("게시글 상세 조회 성공 테스트")
    void getBoard_Success() {
        // 1. Given: Mock 데이터 설정
        Long boardId = 1L;
        Long empId = 100L;
        BoardVO boardVo = BoardVO.builder().boardId(boardId).build();
        BoardResponse boardResponse = BoardResponse.builder().boardId(boardId).build();
        List<BoardCommentVO> comments = Collections.emptyList();
        BoardLikeVo likeVo = new BoardLikeVo(); // 좋아요 눌린 상태

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            when(boardMapper.updateViewCount(boardId)).thenReturn(1);
            when(boardMapper.readBoard(boardId)).thenReturn(boardVo);
            when(boardMapper.readCommentList(boardId)).thenReturn(comments);
            when(boardMapper.readLikeStatus(boardId, empId)).thenReturn(likeVo);
            when(boardMapper.readLikeCount(boardId)).thenReturn(5);

            when(dtoMapper.toDto(any(), eq(BoardResponse.class))).thenReturn(boardResponse);
            // 리스트 변환 모킹
            when(dtoMapper.toDtoList(anyList(), any())).thenReturn(Collections.emptyList());

            // 2. When: 서비스 메서드 호출
            BoardResponse result = boardService.getBoard(null, boardId);

            // 3. Then: 검증
            assertNotNull(result);
            assertEquals(boardId, result.getBoardId());
            assertTrue(result.getIsLiked()); // 좋아요 확인
            assertEquals(5, result.getLikeCnt()); // 좋아요 개수 확인
            
            verify(boardMapper).updateViewCount(boardId);
            verify(boardMapper).readBoard(boardId);
        }
    }

    @Test
    @DisplayName("게시글 존재하지 않을 때 CustomException 발생")
    void getBoard_NotFound() {
        Long boardId = 999L;
        
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentEmpId).thenReturn(100L);
            
            when(boardMapper.updateViewCount(boardId)).thenReturn(0);

            assertThrows(CustomException.class, () -> boardService.getBoard(null, boardId));
        }
    }
}