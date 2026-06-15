package com.mycrewsoft.domain.room.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.room.dto.request.RoomCreateRequest;
import com.mycrewsoft.domain.room.dto.request.RoomUpdateRequest;
import com.mycrewsoft.domain.room.dto.response.RoomResponse;
import com.mycrewsoft.domain.room.mapper.RoomDtoMapper;
import com.mycrewsoft.domain.room.mapper.RoomMapper;
import com.mycrewsoft.domain.room.vo.ConfRmVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private RoomDtoMapper roomDtoMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    @DisplayName("회의실 단건 조회 - 성공")
    void readRoom_success() {
        ConfRmVO vo = new ConfRmVO();
        RoomResponse response = RoomResponse.builder().roomId(1L).build();

        given(roomMapper.selectConfRm(1L)).willReturn(vo);
        given(roomDtoMapper.toResponse(vo)).willReturn(response);

        RoomResponse result = roomService.readRoom(1L);

        assertThat(result.getRoomId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("회의실 단건 조회 - 존재하지 않는 회의실")
    void readRoom_notFound() {
        given(roomMapper.selectConfRm(999L)).willReturn(null);

        assertThatThrownBy(() -> roomService.readRoom(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CONF_RM_NOT_FOUND));
    }

    @Test
    @DisplayName("회의실 목록 조회 - 성공")
    void readRoomList_success() {
        List<ConfRmVO> voList = List.of(new ConfRmVO(), new ConfRmVO());
        List<RoomResponse> responseList = List.of(
                RoomResponse.builder().roomId(1L).build(),
                RoomResponse.builder().roomId(2L).build()
        );

        given(roomMapper.selectConfRmList()).willReturn(voList);
        given(roomDtoMapper.toResponseList(voList)).willReturn(responseList);

        List<RoomResponse> result = roomService.readRoomList();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("회의실 목록 조회 - 빈 목록")
    void readRoomList_empty() {
        given(roomMapper.selectConfRmList()).willReturn(List.of());

        List<RoomResponse> result = roomService.readRoomList();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("회의실 생성 - 성공")
    void createRoom_success() {
        RoomCreateRequest request = new RoomCreateRequest();
        ConfRmVO vo = new ConfRmVO();
        vo.setConfRmId(1L);

        given(roomDtoMapper.toVO(request)).willReturn(vo);
        given(roomMapper.insertConfRm(vo)).willReturn(1);

        Long result = roomService.createRoom(request);

        assertThat(result).isEqualTo(1L);
    }

    @Test
    @DisplayName("회의실 생성 - DB 저장 실패")
    void createRoom_insertFailed() {
        RoomCreateRequest request = new RoomCreateRequest();
        ConfRmVO vo = new ConfRmVO();

        given(roomDtoMapper.toVO(request)).willReturn(vo);
        given(roomMapper.insertConfRm(vo)).willReturn(0);

        assertThatThrownBy(() -> roomService.createRoom(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CONF_RM_CREATE_FAILED));
    }

    @Test
    @DisplayName("회의실 수정 - 성공")
    void modifyRoom_success() {
        RoomUpdateRequest request = new RoomUpdateRequest();
        ConfRmVO vo = new ConfRmVO();

        given(roomDtoMapper.toVO(request)).willReturn(vo);
        given(roomMapper.updateConfRm(vo)).willReturn(1);

        roomService.modifyRoom(request);

        verify(roomMapper).updateConfRm(vo);
    }

    @Test
    @DisplayName("회의실 수정 - 존재하지 않는 회의실")
    void modifyRoom_notFound() {
        RoomUpdateRequest request = new RoomUpdateRequest();
        ConfRmVO vo = new ConfRmVO();

        given(roomDtoMapper.toVO(request)).willReturn(vo);
        given(roomMapper.updateConfRm(vo)).willReturn(0);

        assertThatThrownBy(() -> roomService.modifyRoom(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CONF_RM_NOT_FOUND));
    }

    @Test
    @DisplayName("회의실 삭제 - 성공")
    void deleteRoom_success() {
        ConfRmVO vo = new ConfRmVO();
        vo.setConfRmMngrId(1L);

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            given(roomMapper.selectConfRm(1L)).willReturn(vo);
            given(roomMapper.deleteConfRm(1L)).willReturn(1);

            roomService.deleteRoom(1L);

            verify(roomMapper).deleteConfRm(1L);
        }
    }

    @Test
    @DisplayName("회의실 삭제 - 관리자가 아닌 경우")
    void deleteRoom_accessDenied() {
        ConfRmVO vo = new ConfRmVO();
        vo.setConfRmMngrId(999L);

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            given(roomMapper.selectConfRm(1L)).willReturn(vo);

            assertThatThrownBy(() -> roomService.deleteRoom(1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCESS_DENIED));
        }
    }

    @Test
    @DisplayName("회의실 삭제 - 인증 정보 없음")
    void deleteRoom_unauthorized() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(null);

            assertThatThrownBy(() -> roomService.deleteRoom(1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.UNAUTHORIZED));
        }
    }
}