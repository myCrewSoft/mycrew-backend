package com.mycrewsoft.domain.board.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mycrewsoft.domain.board.dto.request.BoardCommentUpdateRequest;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.board.vo.BoardCommentVO;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.util.SecurityUtil;
import com.mycrewsoft.common.util.DtoMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest7 {

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private DtoMapper dtoMapper;

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
    @DisplayName("댓글 수정 성공 - 본인 댓글이고 데이터가 존재하면 성공적으로 수정 후 댓글 ID를 반환한다")
    void updateComment_Success() {
        // given
        Long loginEmpId = 45L; // Long 타입 사원 ID
        Long mockCommentId = 77L;

        BoardCommentUpdateRequest request = new BoardCommentUpdateRequest();
        request.setCommentId(mockCommentId);
        request.setCommentCn("수정 완벽 반영 테스트");

        BoardCommentVO mockVo = new BoardCommentVO();
        mockVo.setCommentId(mockCommentId);
        mockVo.setCommentCn(request.getCommentCn());

        // 가짜 객체들 행동 정의
        mockedSecurityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(loginEmpId);
        when(dtoMapper.toDto(request, BoardCommentVO.class)).thenReturn(mockVo);
        when(boardMapper.updateComment(any(BoardCommentVO.class))).thenReturn(1); // 1행 수정 성공

        // when
        Long updatedCommentId = boardService.updateComment(request);

        // then
        assertEquals(mockCommentId, updatedCommentId);
        assertEquals(loginEmpId, mockVo.getWrterEmpId()); // ✨ 둘 다 온전한 Long 타입 비교 검증
        
        // 📌 중요: 서비스에서 중복 쿼리를 지웠으므로 이제 DB 업데이트 호출은 딱 1번(times(1))만 일어납니다!
        verify(boardMapper, times(1)).updateComment(mockVo);
    }

    @Test
    @DisplayName("댓글 수정 실패 - 타인의 글이거나 댓글이 없으면 BOARD_NOT_FOUND 예외가 발생한다")
    void updateComment_Fail_NotFound() {
        // given
        Long loginEmpId = 99L;
        Long mockCommentId = 77L;

        BoardCommentUpdateRequest request = new BoardCommentUpdateRequest();
        request.setCommentId(mockCommentId);
        request.setCommentCn("타인의 댓글 수정 시도");

        BoardCommentVO mockVo = new BoardCommentVO();
        mockVo.setCommentId(mockCommentId);

        mockedSecurityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(loginEmpId);
        when(dtoMapper.toDto(request, BoardCommentVO.class)).thenReturn(mockVo);
        when(boardMapper.updateComment(any(BoardCommentVO.class))).thenReturn(0); // 수정 실패(0)

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardService.updateComment(request);
        });

        assertEquals(ErrorCode.BOARD_NOT_FOUND, exception.getErrorCode());
        verify(boardMapper, times(1)).updateComment(mockVo);
    }
}