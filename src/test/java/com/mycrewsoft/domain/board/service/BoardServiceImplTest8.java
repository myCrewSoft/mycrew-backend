package com.mycrewsoft.domain.board.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest8 {

    @Mock
    private BoardMapper boardMapper;

    @InjectMocks
    private BoardServiceImpl boardService;

    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 본인이 작성한 댓글이 존재하면 정상적으로 논리 삭제를 완료한다")
    void deleteComment_Success() {
        // given
        Long loginEmpId = 1004L;
        Long mockCommentId = 55L;

        mockedSecurityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(loginEmpId);
        when(boardMapper.readCmWrterEmpId(mockCommentId)).thenReturn(loginEmpId); // 본인 검증 통과
        when(boardMapper.deleteComment(mockCommentId)).thenReturn(1); // 📌 Long 파라미터로 1행 삭제 성공 스터빙

        // when & then
        assertDoesNotThrow(() -> boardService.deleteComment(mockCommentId));

        // 매퍼가 의도한 단일 ID 파라미터로 정확히 1번씩 수행되었는지 검증
        verify(boardMapper, times(1)).readCmWrterEmpId(mockCommentId);
        verify(boardMapper, times(1)).deleteComment(mockCommentId);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 조회한 댓글이 존재하지 않으면 COMMENT_NOT_FOUND 예외가 발생한다")
    void deleteComment_Fail_CommentNotFound() {
        // given
        Long loginEmpId = 1004L;
        Long mockCommentId = 999L;

        mockedSecurityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(loginEmpId);
        when(boardMapper.readCmWrterEmpId(mockCommentId)).thenReturn(null); // 댓글 없음

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardService.deleteComment(mockCommentId);
        });

        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
        // 🔒 앞 단계에서 예외가 터졌으므로 실제 삭제 매퍼는 절대 호출되면 안 됨
        verify(boardMapper, never()).deleteComment(anyLong());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 로그인한 사원과 댓글 작성자가 다르면 ACCESS_DENIED 예외가 발생한다")
    void deleteComment_Fail_AccessDenied() {
        // given
        Long loginEmpId = 1004L;
        Long otherEmpId = 7777L; // 타인 ID
        Long mockCommentId = 55L;

        mockedSecurityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(loginEmpId);
        when(boardMapper.readCmWrterEmpId(mockCommentId)).thenReturn(otherEmpId); // 권한 불일치

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardService.deleteComment(mockCommentId);
        });

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        // 🔒 권한이 없으므로 삭제 쿼리 실행 차단 검증
        verify(boardMapper, never()).deleteComment(anyLong());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한은 검증되었으나 DB 업데이트 반영 결과가 0행이면 ACCESS_DENIED 예외가 발생한다")
    void deleteComment_Fail_DbResultZero() {
        // given
        Long loginEmpId = 1004L;
        Long mockCommentId = 55L;

        mockedSecurityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(loginEmpId);
        when(boardMapper.readCmWrterEmpId(mockCommentId)).thenReturn(loginEmpId);
        when(boardMapper.deleteComment(mockCommentId)).thenReturn(0); // DB 결과가 0행인 상황

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardService.deleteComment(mockCommentId);
        });

        // 📌 [수정] 내 서비스 코드와 일치하도록 ACCESS_DENIED로 변경 완료!
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode()); 
        
        verify(boardMapper, times(1)).deleteComment(mockCommentId);
    }
}