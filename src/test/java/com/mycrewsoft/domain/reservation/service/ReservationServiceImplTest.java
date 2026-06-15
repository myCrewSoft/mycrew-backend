// package com.mycrewsoft.domain.reservation.service;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.BDDMockito.given;
// import static org.mockito.Mockito.verify;

// import java.time.LocalDateTime;
// import java.util.List;

// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import org.mockito.Mockito;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.mycrewsoft.common.exception.CustomException;
// import com.mycrewsoft.common.exception.ErrorCode;
// import com.mycrewsoft.common.util.DateUtil;
// import com.mycrewsoft.domain.reservation.dto.request.ReservationCreateRequest;
// import com.mycrewsoft.domain.reservation.dto.request.ReservationUpdateRequest;
// import com.mycrewsoft.domain.reservation.dto.response.ReservationResponse;
// import com.mycrewsoft.domain.reservation.mapper.ReservationDtoMapper;
// import com.mycrewsoft.domain.reservation.mapper.ReservationMapper;
// import com.mycrewsoft.domain.reservation.vo.ConfRmRsrvVO;
// import com.mycrewsoft.domain.reservation.vo.ReservationDetailVO;
// import com.mycrewsoft.security.util.SecurityUtil;

// @ExtendWith(MockitoExtension.class)
// class ReservationServiceImplTest {

//     @Mock
//     private ReservationMapper reservationMapper;

//     @Mock
//     private ReservationDtoMapper reservationDtoMapper;

//     @InjectMocks
//     private ReservationServiceImpl reservationService;

//     @Test
//     @DisplayName("예약 단건 조회 - 성공")
//     void readReservation_success() {
//         ReservationDetailVO vo = new ReservationDetailVO();
//         vo.setRsrvEmpId(1L);
//         ReservationResponse response = ReservationResponse.builder().reservationId(1L).build();

//         given(reservationMapper.selectConfRmRsrv(1L)).willReturn(vo);
//         given(reservationDtoMapper.toResponse(vo)).willReturn(response);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

//             ReservationResponse result = reservationService.readReservation(1L);

//             assertThat(result.getReservationId()).isEqualTo(1L);
//             assertThat(result.getMine()).isTrue();
//         }
//     }

//     @Test
//     @DisplayName("예약 단건 조회 - 존재하지 않는 예약")
//     void readReservation_notFound() {
//         given(reservationMapper.selectConfRmRsrv(999L)).willReturn(null);

//         assertThatThrownBy(() -> reservationService.readReservation(999L))
//                 .isInstanceOf(CustomException.class)
//                 .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                         .isEqualTo(ErrorCode.RSRV_NOT_FOUND));
//     }

//     @Test
//     @DisplayName("예약 목록 조회 - 성공")
//     void readReservationList_success() {
//         ReservationDetailVO vo1 = new ReservationDetailVO();
//         vo1.setRsrvEmpId(1L);
//         ReservationDetailVO vo2 = new ReservationDetailVO();
//         vo2.setRsrvEmpId(2L);
//         List<ReservationDetailVO> voList = List.of(vo1, vo2);

//         ReservationResponse response1 = ReservationResponse.builder().reservationId(1L).build();
//         ReservationResponse response2 = ReservationResponse.builder().reservationId(2L).build();

//         given(reservationMapper.selectConfRmRsrvList(
//                 LocalDateTime.of(2025, 6, 1, 0, 0),
//                 LocalDateTime.of(2025, 6, 7, 23, 59)
//         )).willReturn(voList);
//         given(reservationDtoMapper.toResponse(vo1)).willReturn(response1);
//         given(reservationDtoMapper.toResponse(vo2)).willReturn(response2);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class);
//              MockedStatic<DateUtil> dateUtil = Mockito.mockStatic(DateUtil.class)) {

//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//             dateUtil.when(() -> DateUtil.parseDateTime("2025-06-01")).thenReturn(LocalDateTime.of(2025, 6, 1, 0, 0));
//             dateUtil.when(() -> DateUtil.parseDateTime("2025-06-07")).thenReturn(LocalDateTime.of(2025, 6, 7, 23, 59));

//             List<ReservationResponse> result = reservationService.readReservationList("2025-06-01", "2025-06-07");

//             assertThat(result).hasSize(2);
//             assertThat(result.get(0).getMine()).isTrue();
//             assertThat(result.get(1).getMine()).isFalse();
//         }
//     }

//     @Test
//     @DisplayName("예약 생성 - 성공")
//     void createReservation_success() {
//         ReservationCreateRequest request = new ReservationCreateRequest(
//                 1L, "주간 회의", "N",
//                 LocalDateTime.of(2025, 6, 5, 9, 0),
//                 LocalDateTime.of(2025, 6, 5, 10, 0)
                
//         );
//         ConfRmRsrvVO vo = new ConfRmRsrvVO();
//         vo.setConfRmId(1L);
//         vo.setBeginDt(request.getStartDateTime());
//         vo.setEndDt(request.getEndDateTime());
//         vo.setRsrvId(1L);

//         given(reservationDtoMapper.toVo(request)).willReturn(vo);
//         given(reservationMapper.countOverlappingRsrv(1L, vo.getBeginDt(), vo.getEndDt(), 0L)).willReturn(0);
//         given(reservationMapper.insertConfRmRsrv(vo)).willReturn(1);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

//             Long result = reservationService.createReservation(request);

//             assertThat(result).isEqualTo(1L);
//         }
//     }

//     @Test
//     @DisplayName("예약 생성 - 시간 중복")
//     void createReservation_timeConflict() {
//         ReservationCreateRequest request = new ReservationCreateRequest(
//                 1L, "주간 회의", "N",
//                 LocalDateTime.of(2025, 6, 5, 9, 0),
//                 LocalDateTime.of(2025, 6, 5, 10, 0)
//         );
//         ConfRmRsrvVO vo = new ConfRmRsrvVO();
//         vo.setConfRmId(1L);
//         vo.setBeginDt(request.getStartDateTime());
//         vo.setEndDt(request.getEndDateTime());

//         given(reservationDtoMapper.toVo(request)).willReturn(vo);
//         given(reservationMapper.countOverlappingRsrv(1L, vo.getBeginDt(), vo.getEndDt(), 0L)).willReturn(1);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

//             assertThatThrownBy(() -> reservationService.createReservation(request))
//                     .isInstanceOf(CustomException.class)
//                     .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                             .isEqualTo(ErrorCode.RSRV_TIME_CONFLICT));
//         }
//     }

//     @Test
//     @DisplayName("예약 생성 - 종일 예약 시 시간 자동 설정")
//     void createReservation_allDay() {
//         ReservationCreateRequest request = new ReservationCreateRequest(
//                 1L, "전사 회의", "Y",
//                 LocalDateTime.of(2025, 6, 5, 9, 0),
//                 LocalDateTime.of(2025, 6, 5, 10, 0)
//         );
//         ConfRmRsrvVO vo = new ConfRmRsrvVO();
//         vo.setConfRmId(1L);
//         vo.setBeginDt(request.getStartDateTime());
//         vo.setEndDt(request.getEndDateTime());
//         vo.setRsrvId(1L);

//         given(reservationDtoMapper.toVo(request)).willReturn(vo);
//         given(reservationMapper.countOverlappingRsrv(
//                 1L,
//                 LocalDateTime.of(2025, 6, 5, 0, 0),
//                 LocalDateTime.of(2025, 6, 5, 23, 59),
//                 0L
//         )).willReturn(0);
//         given(reservationMapper.insertConfRmRsrv(vo)).willReturn(1);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

//             reservationService.createReservation(request);

//             assertThat(vo.getBeginDt()).isEqualTo(LocalDateTime.of(2025, 6, 5, 0, 0));
//             assertThat(vo.getEndDt()).isEqualTo(LocalDateTime.of(2025, 6, 5, 23, 59));
//         }
//     }

//     @Test
//     @DisplayName("예약 수정 - 성공")
//     void modifyReservation_success() {
//         ReservationDetailVO existing = new ReservationDetailVO();
//         existing.setRsrvEmpId(1L);

//         ReservationUpdateRequest request = new ReservationUpdateRequest(
//                 1L, "수정된 회의", "N", 
//                 LocalDateTime.of(2025, 6, 5, 10, 0),
//                 LocalDateTime.of(2025, 6, 5, 11, 0)
//         );
//         ConfRmRsrvVO vo = new ConfRmRsrvVO();
//         vo.setConfRmId(1L);
//         vo.setBeginDt(request.getStartDateTime());
//         vo.setEndDt(request.getEndDateTime());

//         given(reservationMapper.selectConfRmRsrv(1L)).willReturn(existing);
//         given(reservationDtoMapper.toVo(request)).willReturn(vo);
//         given(reservationMapper.countOverlappingRsrv(1L, vo.getBeginDt(), vo.getEndDt(), 1L)).willReturn(0);
//         given(reservationMapper.updateConfRmRsrv(vo)).willReturn(1);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

//             reservationService.modifyReservation(1L, request);

//             verify(reservationMapper).updateConfRmRsrv(vo);
//         }
//     }

//     @Test
//     @DisplayName("예약 수정 - 본인 예약 아닌 경우")
//     void modifyReservation_accessDenied() {
//         ReservationDetailVO existing = new ReservationDetailVO();
//         existing.setRsrvEmpId(999L);

//         given(reservationMapper.selectConfRmRsrv(1L)).willReturn(existing);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

//             assertThatThrownBy(() -> reservationService.modifyReservation(1L, new ReservationUpdateRequest()))
//                     .isInstanceOf(CustomException.class)
//                     .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                             .isEqualTo(ErrorCode.ACCESS_DENIED));
//         }
//     }

//     @Test
//     @DisplayName("예약 삭제 - 성공")
//     void deleteReservation_success() {
//         ReservationDetailVO existing = new ReservationDetailVO();
//         existing.setRsrvEmpId(1L);

//         given(reservationMapper.selectConfRmRsrv(1L)).willReturn(existing);
//         given(reservationMapper.deleteConfRmRsrv(1L)).willReturn(1);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

//             reservationService.deleteReservation(1L);

//             verify(reservationMapper).deleteConfRmRsrv(1L);
//         }
//     }

//     @Test
//     @DisplayName("예약 삭제 - 존재하지 않는 예약")
//     void deleteReservation_notFound() {
//         given(reservationMapper.selectConfRmRsrv(999L)).willReturn(null);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

//             assertThatThrownBy(() -> reservationService.deleteReservation(999L))
//                     .isInstanceOf(CustomException.class)
//                     .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                             .isEqualTo(ErrorCode.RSRV_NOT_FOUND));
//         }
//     }

//     @Test
//     @DisplayName("예약 삭제 - 본인 예약 아닌 경우")
//     void deleteReservation_accessDenied() {
//         ReservationDetailVO existing = new ReservationDetailVO();
//         existing.setRsrvEmpId(999L);

//         given(reservationMapper.selectConfRmRsrv(1L)).willReturn(existing);

//         try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
//             securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

//             assertThatThrownBy(() -> reservationService.deleteReservation(1L))
//                     .isInstanceOf(CustomException.class)
//                     .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                             .isEqualTo(ErrorCode.ACCESS_DENIED));
//         }
//     }
// }