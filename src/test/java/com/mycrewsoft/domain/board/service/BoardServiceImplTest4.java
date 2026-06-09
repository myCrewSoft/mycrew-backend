package com.mycrewsoft.domain.board.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.board.dto.request.BoardUpdateRequest;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.board.vo.BoardVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest4 {

    @InjectMocks
    private BoardServiceImpl boardService; // 실제 서비스 구현체 클래스명으로 변경하세요.

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private DtoMapper dtoMapper;

    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        // 정적(Static) 메서드 모킹 시작
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        // 정적 메서드 모킹 종료 (메모리 누수 및 다른 테스트 영향 방지)
        mockedSecurityUtil.close();
    }

    @Test
    @DisplayName("게시글 수정 성공 - 작성자 본인이 요청한 경우")
    void updateBoardDetail_Success() {
        // given
        Long empId = 100L;
        Long boardId = 1L;

        BoardUpdateRequest request = new BoardUpdateRequest();
        request.setBoardId(boardId);

        BoardVO existingBoard = new BoardVO();
        existingBoard.setFrstRgtrId(empId); // DB에 저장된 작성자 ID를 로그인한 사원 ID와 일치시킴

        BoardVO convertedBoard = new BoardVO();
        convertedBoard.setBoardId(boardId);

        // Mock 가짜 행동 정의
        when(SecurityUtil.getCurrentEmpId()).thenReturn(empId);
        when(boardMapper.readBoard(boardId)).thenReturn(existingBoard);
        when(dtoMapper.toDto(request, BoardVO.class)).thenReturn(convertedBoard);

        // when
        Long updatedBoardId = boardService.updateBoardDetail(request);

        // then
        assertEquals(boardId, updatedBoardId);
        verify(boardMapper, times(1)).updateBoardDetails(convertedBoard); // DB 업데이트 메서드가 실제 실행되었는지 검증
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아닌 사원이 요청한 경우 예외 발생")
    void updateBoardDetail_ThrowsException_WhenNotWriter() {
        // given
        Long empId = 100L;       // 로그인한 사원
        Long writerId = 999L;    // 실제 게시글 작성자 (서로 다름)
        Long boardId = 1L;

        BoardUpdateRequest request = new BoardUpdateRequest();
        request.setBoardId(boardId);

        BoardVO existingBoard = new BoardVO();
        existingBoard.setFrstRgtrId(writerId); // 작성자 ID 다르게 설정

        // Mock 가짜 행동 정의
        when(SecurityUtil.getCurrentEmpId()).thenReturn(empId);
        when(boardMapper.readBoard(boardId)).thenReturn(existingBoard);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardService.updateBoardDetail(request);
        });

        // 예외 코드 및 메시지 검증 (프로젝트의 ErrorCode 구조에 맞게 수정하세요)
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode()); 
        
        // 권한이 없으므로 후속 로직(매핑 및 수정)이 절대 실행되지 않아야 함을 검증
        verify(dtoMapper, never()).toDto(any(), any());
        verify(boardMapper, never()).updateBoardDetails(any());
    }
}