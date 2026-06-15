package com.mycrewsoft.domain.board.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.board.vo.BoardLikeVo;

@ExtendWith(MockitoExtension.class) // Mockito 가짜 객체 사용을 위한 설정
class BoardServiceImplTest9 {

    @Mock
    private BoardMapper boardMapper; // 가짜 매퍼 객체 생성

    @InjectMocks
    private BoardServiceImpl boardService; // 가짜 매퍼가 주입된 실제 서비스 객체

    private final Long boardId = 1L;
    private final Long empId = 100L;

    @Nested
    @DisplayName("toggelLike (좋아요 토글) 테스트")
    class ToggleLikeTest {

        @Test
        @DisplayName("성공: 기존에 좋아요가 없다면(null), 새 좋아요를 등록하고 true를 반환한다")
        void toggleLike_insert_success() {
            // given (준비): 매퍼가 readLikeStatus를 호출하면 null을 반환하라고 가짜로 설정
            when(boardMapper.readLikeStatus(boardId, empId)).thenReturn(null);

            // when (실행): 서비스 메서드 호출
            boolean result = boardService.toggelLike(boardId, empId);

            // then (검증): 결과가 true여야 함
            assertTrue(result);
            
            // 매퍼의 insertLike가 실제로 1번 실행되었는지 검증
            verify(boardMapper, times(1)).insertLike(any(BoardLikeVo.class));
            // deleteLike는 실행되면 안 됨
            verify(boardMapper, never()).deleteLike(anyLong(), anyLong());
        }

        @Test
        @DisplayName("성공: 기존에 좋아요가 있다면, 좋아요를 삭제하고 false를 반환한다")
        void toggleLike_delete_success() {
            // given (준비): 기존에 이미 누른 데이터가 존재한다고 가짜 객체 세팅
            BoardLikeVo existingLike = new BoardLikeVo();
            existingLike.setBoardId(boardId);
            existingLike.setEmpId(empId);
            
            when(boardMapper.readLikeStatus(boardId, empId)).thenReturn(existingLike);

            // when (실행)
            boolean result = boardService.toggelLike(boardId, empId);

            // then (검증): 결과가 false여야 함
            assertFalse(result);
            
            // 매퍼의 deleteLike가 실제로 1번 실행되었는지 검증
            verify(boardMapper, times(1)).deleteLike(boardId, empId);
            // insertLike는 실행되면 안 됨
            verify(boardMapper, never()).insertLike(any(BoardLikeVo.class));
        }
    }

    @Nested
    @DisplayName("getLike (좋아요 상태 및 개수 조회) 테스트")
    class GetLikeTest {

        @Test
        @DisplayName("성공: 총 개수와 로그인 유저의 좋아요 상태(true)를 Map에 담아 반환한다")
        void getLike_liked_user() {
            // given (준비)
            int expectedCount = 5;
            BoardLikeVo existingLike = new BoardLikeVo(); // null이 아닌 상태
            
            when(boardMapper.readLikeCount(boardId)).thenReturn(expectedCount);
            when(boardMapper.readLikeStatus(boardId, empId)).thenReturn(existingLike);

            // when (실행)
            Map<String, Object> result = boardService.getLike(boardId, empId);

            // then (검증)
            assertNotNull(result);
            assertEquals(expectedCount, result.get("likeCount")); // 개수가 5개인지 확인
            assertEquals(true, result.get("isLiked"));           // 내가 눌렀으니 true인지 확인
        }

        @Test
        @DisplayName("성공: 총 개수와 로그인 유저의 좋아요 상태(false)를 Map에 담아 반환한다")
        void getLike_not_liked_user() {
            // given (준비)
            int expectedCount = 3;
            
            when(boardMapper.readLikeCount(boardId)).thenReturn(expectedCount);
            when(boardMapper.readLikeStatus(boardId, empId)).thenReturn(null); // 누른 적 없음(null)

            // when (실행)
            Map<String, Object> result = boardService.getLike(boardId, empId);

            // then (검증)
            assertNotNull(result);
            assertEquals(expectedCount, result.get("likeCount")); // 개수가 3개인지 확인
            assertEquals(false, result.get("isLiked"));          // 안 눌렀으니 false인지 확인
        }
    }
}