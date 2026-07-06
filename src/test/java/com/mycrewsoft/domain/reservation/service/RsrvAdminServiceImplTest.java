//package com.mycrewsoft.domain.reservation.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//
//import java.util.List;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import com.mycrewsoft.common.exception.CustomException;
//import com.mycrewsoft.domain.reservation.dto.response.PopularRmItem;
//import com.mycrewsoft.domain.reservation.dto.response.RsrvListItem;
//import com.mycrewsoft.domain.reservation.dto.response.RsrvStatsSummary;
//import com.mycrewsoft.domain.reservation.mapper.ReservationDtoMapper;
//import com.mycrewsoft.domain.reservation.mapper.ReservationMapper;
//import com.mycrewsoft.domain.reservation.mapper.RsrvAdminMapper;
//import com.mycrewsoft.domain.reservation.vo.PopularRmVO;
//import com.mycrewsoft.domain.reservation.vo.ReservationDetailVO;
//import com.mycrewsoft.domain.reservation.vo.RsrvAdminVO;
//import com.mycrewsoft.domain.reservation.vo.RsrvSearchVO;
//import com.mycrewsoft.domain.reservation.vo.RsrvSummaryVO;
//
//@ExtendWith(MockitoExtension.class)
//class RsrvAdminServiceImplTest {
//
//	@InjectMocks
//	private RsrvAdminServiceImpl rsrvAdminService;
//
//	@Mock
//	private RsrvAdminMapper rsrvAdminMapper;
//
//	@Mock
//	private ReservationMapper reservationMapper;
//
//	@Mock
//	private ReservationDtoMapper reservationDtoMapper;
//
//	private RsrvSummaryVO summaryVO;
//	private PopularRmVO popularRmVO;
//	private PopularRmItem popularRmItem;
//	private RsrvAdminVO rsrvAdminVO;
//	private RsrvListItem rsrvListItem;
//	private ReservationDetailVO rsrvDetailVO;
//
//	@BeforeEach
//	void setUp() {
//		summaryVO = new RsrvSummaryVO();
//		setField(summaryVO, "todayRsrvCount", 5);
//		setField(summaryVO, "weekRsrvCount", 20);
//		setField(summaryVO, "availableRmCount", 3);
//
//		popularRmVO = new PopularRmVO();
//		setField(popularRmVO, "confRmNm", "대회의실");
//		setField(popularRmVO, "rsrvCount", 8);
//
//		popularRmItem = PopularRmItem.builder().confRmNm("대회의실").rsrvCount(8).build();
//
//		rsrvAdminVO = new RsrvAdminVO();
//		setField(rsrvAdminVO, "rsrvId", 1L);
//		setField(rsrvAdminVO, "confRmNm", "대회의실");
//		setField(rsrvAdminVO, "rsrvEmpNm", "김철수");
//		setField(rsrvAdminVO, "delYn", "N");
//
//		rsrvListItem = RsrvListItem.builder().rsrvId(1L).confRmNm("대회의실").rsrvEmpNm("김철수").delYn("N").build();
//
//		rsrvDetailVO = new ReservationDetailVO();
//		setField(rsrvDetailVO, "rsrvId", 1L);
//		setField(rsrvDetailVO, "delYn", "N");
//	}
//
//	private void setField(Object target, String fieldName, Object value) {
//		try {
//			java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
//			field.setAccessible(true);
//			field.set(target, value);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@Test
//	void 예약_운영현황_조회_성공() {
//		given(rsrvAdminMapper.selectRsrvStatsSummary()).willReturn(summaryVO);
//		given(rsrvAdminMapper.selectPopularRmList()).willReturn(List.of(popularRmVO));
//		given(reservationDtoMapper.toPopularRmItem(popularRmVO)).willReturn(popularRmItem);
//
//		RsrvStatsSummary result = rsrvAdminService.readRsrvStatsSummary();
//
//		assertThat(result.getTodayRsrvCount()).isEqualTo(5);
//		assertThat(result.getWeekRsrvCount()).isEqualTo(20);
//		assertThat(result.getAvailableRmCount()).isEqualTo(3);
//		assertThat(result.getPopularRmList()).hasSize(1);
//		assertThat(result.getPopularRmList().get(0).getConfRmNm()).isEqualTo("대회의실");
//	}
//
//	@Test
//	void 예약_목록_조회_성공() {
//		RsrvSearchVO searchVO = RsrvSearchVO.builder().build();
//		given(rsrvAdminMapper.selectRsrvList(searchVO)).willReturn(List.of(rsrvAdminVO));
//		given(reservationDtoMapper.toRsrvListItem(rsrvAdminVO)).willReturn(rsrvListItem);
//
//		List<RsrvListItem> result = rsrvAdminService.readRsrvList(searchVO);
//
//		assertThat(result).hasSize(1);
//		assertThat(result.get(0).getConfRmNm()).isEqualTo("대회의실");
//		assertThat(result.get(0).getRsrvEmpNm()).isEqualTo("김철수");
//	}
//
//	@Test
//	void 예약_목록_없을때_빈_리스트_반환() {
//		RsrvSearchVO searchVO = RsrvSearchVO.builder().build();
//		given(rsrvAdminMapper.selectRsrvList(searchVO)).willReturn(List.of());
//
//		List<RsrvListItem> result = rsrvAdminService.readRsrvList(searchVO);
//
//		assertThat(result).isEmpty();
//	}
//
//	@Test
//	void 예약_강제취소_성공() {
//		given(reservationMapper.selectConfRmRsrv(1L)).willReturn(rsrvDetailVO);
//		given(reservationMapper.deleteConfRmRsrv(1L)).willReturn(1);
//
//		rsrvAdminService.cancelRsrv(1L);
//
//		then(reservationMapper).should().deleteConfRmRsrv(1L);
//	}
//
//	@Test
//	void 예약_강제취소_존재하지않는예약_실패() {
//		given(reservationMapper.selectConfRmRsrv(99L)).willReturn(null);
//
//		assertThatThrownBy(() -> rsrvAdminService.cancelRsrv(99L)).isInstanceOf(CustomException.class);
//	}
//
//	@Test
//	void 예약_강제취소_이미취소된예약_실패() {
//		setField(rsrvDetailVO, "delYn", "Y");
//		given(reservationMapper.selectConfRmRsrv(1L)).willReturn(rsrvDetailVO);
//
//		assertThatThrownBy(() -> rsrvAdminService.cancelRsrv(1L)).isInstanceOf(CustomException.class);
//	}
//}
