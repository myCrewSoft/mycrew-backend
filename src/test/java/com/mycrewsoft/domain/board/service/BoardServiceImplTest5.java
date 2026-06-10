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
import com.mycrewsoft.domain.board.vo.BoardVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest5 {

    @Mock
    private BoardMapper boardMapper;

    @InjectMocks
    private BoardServiceImpl boardService; // 실제 구현체 클래스명으로 변경해 주세요.

    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        // SecurityUtil.getCurrentEmpId()가 static 메서드이므로 MockedStatic을 사용해 모킹 준비를 합니다.
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        // static 모킹은 테스트가 끝난 후 반드시 닫아주어야 다른 테스트에 영향을 주지 않습니다.
        mockedSecurityUtil.close();
    }

    @Test
    @DisplayName("게시글 삭제 성공 - 본인이 작성한 글인 경우 정상적으로 논리 삭제된다")
    void deleteBoardDetail_Success() {
        // given
        Long boardId = 100L;
        Long loginEmpId = 2L;      // 로그인한 사람 ID: 2
        Long writerEmpId = 2L;     // 작성자 ID: 2 (일치)

        // 가짜 게시글 데이터 생성
        BoardVO mockBoard = new BoardVO();
        mockBoard.setBoardId(boardId);
        mockBoard.setFrstRgtrId(writerEmpId);

        // Mock 객체 행동 정의 (Stubbing)
        mockedSecurityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(loginEmpId);
        when(boardMapper.readBoard(boardId)).thenReturn(mockBoard);
        when(boardMapper.deleteBoardDetail(boardId)).thenReturn(1); // 성공 시 1 반환 가정

        // when & then (예외가 발생하지 않고 정상 종료되는지 확인)
        assertDoesNotThrow(() -> boardService.deleteBoardDetail(boardId));

        // 검증: 매퍼의 삭제 메서드가 실제로 1번 호출되었는지 체크
        verify(boardMapper, times(1)).deleteBoardDetail(boardId);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 타인이 작성한 글인 경우 ACCESS_DENIED 예외가 발생한다")
    void deleteBoardDetail_Fail_AccessDenied() {
        // given
        Long boardId = 100L;
        Long loginEmpId = 3L;      // 로그인한 사람 ID: 3
        Long writerEmpId = 2L;     // 작성자 ID: 2 (불일치!)

        BoardVO mockBoard = new BoardVO();
        mockBoard.setBoardId(boardId);
        mockBoard.setFrstRgtrId(writerEmpId);

        mockedSecurityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(loginEmpId);
        when(boardMapper.readBoard(boardId)).thenReturn(mockBoard);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardService.deleteBoardDetail(boardId);
        });

        // 내가 지정한 에러코드가 맞는지 검증
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        
        // 권한 검증에서 팅겼으므로 실제 삭제 매퍼 함수는 호출되지 않아야 함을 검증
        verify(boardMapper, never()).deleteBoardDetail(anyLong());
    }
}