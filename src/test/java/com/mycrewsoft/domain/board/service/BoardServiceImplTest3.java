package com.mycrewsoft.domain.board.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.board.dto.request.BoardCreateRequest;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.board.vo.BoardVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // Mockito 기능을 JUnit 5에 확장 적용
class BoardServiceImplTest3 {

    @Mock
    private BoardMapper boardMapper; // 가짜 매퍼 주입

    @Mock
    private DtoMapper dtoMapper; // 가짜 DtoMapper 주입

    @InjectMocks
    private BoardServiceImpl boardService; // 테스트 대상 서비스 클래스 (인터페이스 구현체)

    @Test
    @DisplayName("게시글 생성 성공 - 정상적으로 DTO를 VO로 변환하고 생성된 boardId를 반환한다")
    void createBoard_Success() {
        // 1. Given (테스트에 필요한 가짜 데이터 및 Mock 행동 정의)
        BoardCreateRequest request = new BoardCreateRequest();
        request.setBoardTypeCd("FREE");
        request.setBoardSj("테스트 제목");
        request.setBoardCn("테스트 내용");

        BoardVO mockVo = new BoardVO();
        mockVo.setBoardTypeCd("FREE");
        mockVo.setBoardSj("테스트 제목");
        mockVo.setBoardCn("테스트 내용");
        // boardId는 처음엔 null 상태

        Long expectedBoardId = 100L;

        // [Mock 행동 정의 1] dtoMapper.toDto가 호출되면 mockVo를 리턴해라
        when(dtoMapper.toDto(request, BoardVO.class)).thenReturn(mockVo);

        // [Mock 행동 정의 2] boardMapper.createBoard가 실행될 때 오라클 시퀀스처럼 boardId를 채워주는 동작 흉내내기
        doAnswer(invocation -> {
            BoardVO vo = invocation.getArgument(0);
            vo.setBoardId(expectedBoardId); // MyBatis selectKey가 주는 효과를 흉내냄
            return null;
        }).when(boardMapper).createBoard(mockVo);

        // 2. When (실제 테스트할 메서드 실행)
        Long actualBoardId = boardService.createBoard(request);

        // 3. Then (결과 검증)
        assertNotNull(actualBoardId);
        assertEquals(expectedBoardId, actualBoardId, "반환된 게시글 ID가 예상치와 일치해야 합니다.");

        // 각 가짜 객체의 메서드들이 실제로 누락 없이 호출되었는지 검증
        verify(dtoMapper, times(1)).toDto(request, BoardVO.class);
        verify(boardMapper, times(1)).createBoard(mockVo);
    }
}