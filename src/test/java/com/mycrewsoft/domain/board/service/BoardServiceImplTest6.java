package com.mycrewsoft.domain.board.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
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

import com.mycrewsoft.common.util.DtoMapper; // 프로젝트에서 사용하는 DtoMapper 클래스 경로로 수정 필요
import com.mycrewsoft.domain.board.dto.request.BoardCommentCreateRequest;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.board.vo.BoardCommentVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest6 {

    @Mock
    private BoardMapper boardMapper; // 작성하신 코드의 boardMapper 변수명과 매칭

    @Mock
    private DtoMapper dtoMapper; // DTO 변환기 모킹

    @InjectMocks
    private BoardServiceImpl boardCommentService; // 실제 구현체 클래스명에 맞게 수정해주세요.

    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        // SecurityUtil.getCurrentEmpId()가 static 메서드이므로 mockStatic으로 모킹 준비
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        // static 모킹은 테스트 종료 후 반드시 닫아주어야 메모리 누수 및 다른 테스트 오작동을 막습니다.
        mockedSecurityUtil.close();
    }

    @Test
    @DisplayName("댓글 생성 성공 - 올바른 DTO가 주어지면 작성자 ID를 세팅하고 정상적으로 댓글이 등록된다")
    void createComment_Success() {
        // given
        Long mockEmpId = 45L;      // 세션에서 꺼내올 가짜 사원 ID
        Long mockBoardId = 100L;    // 가짜 게시글 ID
        Long mockCommentId = 5L;    // DB에서 시퀀스로 생성될 가짜 댓글 ID

        // 1. 요청 DTO 객체 생성
        BoardCommentCreateRequest request = new BoardCommentCreateRequest();
        request.setBoardId(mockBoardId);
        request.setCommentCn("테스트 댓글 내용입니다.");

        // 2. 변환 결과로 나올 가짜 VO 객체 생성
        BoardCommentVO mockVO = new BoardCommentVO();
        mockVO.setBoardId(mockBoardId);
        mockVO.setCommentCn(request.getCommentCn());

        // Mock 객체들의 행동 정의 (Stubbing)
        mockedSecurityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(mockEmpId);
        
        // dtoMapper가 해당 request를 BoardCommentVO.class 유형으로 변환할 때 가짜 VO를 반환하도록 설정
        when(dtoMapper.toDto(request, BoardCommentVO.class)).thenReturn(mockVO);
        
        // boardMapper.insertComment가 실행될 때, 시퀀스 값을 시뮬레이션하기 위해 가짜 VO에 commentId와 boardId 주입
        doAnswer(invocation -> {
            BoardCommentVO vo = invocation.getArgument(0);
            vo.setCommentId(mockCommentId); // XML의 useGeneratedKeys 효과를 모방
            return null;
        }).when(boardMapper).insertComment(any(BoardCommentVO.class));

        // when
        Long resultCommentId = boardCommentService.createComment(request);

        // then
        // 1. 작성자 사원 ID가 VO에 정상적으로 세팅되었는지 검증
        assertEquals(mockEmpId, mockVO.getWrterEmpId());
        
        // 2. 생성된 댓글 ID가 반환되는지 검증
        assertEquals(mockCommentId, resultCommentId);

        // 3. 실제 매퍼의 insertComment 메서드가 1번 호출되었는지 검증
        verify(boardMapper, times(1)).insertComment(mockVO);
    }
}
