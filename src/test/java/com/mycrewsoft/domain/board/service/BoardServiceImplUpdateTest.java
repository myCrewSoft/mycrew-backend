package com.mycrewsoft.domain.board.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.board.dto.request.BoardUpdateRequest;
import com.mycrewsoft.domain.board.mapper.BoardMapper;
import com.mycrewsoft.domain.board.vo.BoardVO;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplUpdateTest {

	@Mock
	private EmployeeMapper employeeMapper;

	@Mock
	private BoardMapper boardMapper;

	@Mock
	private AuthorizationService authorizationService;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private DtoMapper dtoMapper;

	@InjectMocks
	private BoardServiceImpl boardService;

	@Test
	void updateBoardDetail_setsPathBoardIdBeforeUpdate() {
		Long boardId = 85L;
		Long empId = 2L;
		BoardUpdateRequest request = new BoardUpdateRequest();

		BoardVO existingBoard = new BoardVO();
		existingBoard.setFrstRgtrId(empId);

		BoardVO updateBoard = new BoardVO();

		try (var securityUtil = mockStatic(SecurityUtil.class)) {
			securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
			when(boardMapper.readBoard(boardId)).thenReturn(existingBoard);
			when(dtoMapper.toDto(request, BoardVO.class)).thenReturn(updateBoard);
			when(boardMapper.updateBoardDetails(updateBoard)).thenReturn(1);

			Long result = boardService.updateBoardDetail(boardId, request);

			ArgumentCaptor<BoardVO> captor = ArgumentCaptor.forClass(BoardVO.class);
			verify(boardMapper).updateBoardDetails(captor.capture());
			assertEquals(boardId, captor.getValue().getBoardId());
			assertEquals(boardId, result);
		}
	}

	@Test
	void updateBoardDetail_throwsBoardNotFoundWhenBoardDoesNotExist() {
		Long boardId = 85L;
		BoardUpdateRequest request = new BoardUpdateRequest();

		try (var securityUtil = mockStatic(SecurityUtil.class)) {
			securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(2L);
			when(boardMapper.readBoard(boardId)).thenReturn(null);

			CustomException exception = assertThrows(
					CustomException.class,
					() -> boardService.updateBoardDetail(boardId, request));

			assertEquals(ErrorCode.BOARD_NOT_FOUND, exception.getErrorCode());
			verify(dtoMapper, never()).toDto(request, BoardVO.class);
			verify(boardMapper, never()).updateBoardDetails(org.mockito.ArgumentMatchers.any());
		}
	}

}
