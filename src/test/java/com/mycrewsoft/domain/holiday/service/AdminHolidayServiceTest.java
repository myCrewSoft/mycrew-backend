package com.mycrewsoft.domain.holiday.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.holiday.client.HolidayApiClient;
import com.mycrewsoft.domain.holiday.dto.request.HolidayManualRequest;
import com.mycrewsoft.domain.holiday.dto.response.HolidayResponse;
import com.mycrewsoft.domain.holiday.mapper.HolidayDtoMapper;
import com.mycrewsoft.domain.holiday.mapper.HolidayMapper;
import com.mycrewsoft.domain.holiday.vo.HolidayVO;

@SpringBootTest
@Transactional
@DisplayName("관리자 공휴일 서비스 단위 테스트")
class AdminHolidayServiceTest {

    @Autowired
    private AdminHolidayService adminHolidayService;

    @MockBean private HolidayMapper holidayMapper;
    @MockBean private HolidayApiClient holidayApiClient;
    @MockBean private HolidayDtoMapper holidayDtoMapper;

    // ─────────────────────────────────────────────────────────────
    //  목록 조회
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 성공")
    void getHolidayList_success() {

        HolidayVO vo = createHolidayVO(1L, "신정", "API");
        HolidayResponse response = createHolidayResponse(1L, "신정", "API");

        given(holidayMapper.selectHolidayList(2026)).willReturn(List.of(vo));
        given(holidayDtoMapper.toResponseList(List.of(vo))).willReturn(List.of(response));

        List<HolidayResponse> result = adminHolidayService.getHolidayList(2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHolidayNm()).isEqualTo("신정");
    }

    // ─────────────────────────────────────────────────────────────
    //  단건 조회
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 성공")
    void getHoliday_success() {

        HolidayVO vo = createHolidayVO(1L, "신정", "API");
        HolidayResponse response = createHolidayResponse(1L, "신정", "API");

        given(holidayMapper.selectHoliday(1L)).willReturn(vo);
        given(holidayDtoMapper.toResponse(vo)).willReturn(response);

        HolidayResponse result = adminHolidayService.getHoliday(1L);

        assertThat(result).isNotNull();
        assertThat(result.getHolidayNm()).isEqualTo("신정");
    }

    @Test
    @DisplayName("단건 조회 실패 - 존재하지 않는 공휴일")
    void getHoliday_notFound_fail() {

        given(holidayMapper.selectHoliday(999L)).willReturn(null);

        assertThatThrownBy(() -> adminHolidayService.getHoliday(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.HOLIDAY_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────────────────────
    //  API 동기화
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("API 동기화 성공 - 처리 건수 반환")
    void syncHolidays_success() {

        List<HolidayVO> apiResult = List.of(
                createHolidayVO(null, "신정", "API"),
                createHolidayVO(null, "설날", "API"),
                createHolidayVO(null, "삼일절", "API")
        );

        given(holidayApiClient.fetchHolidays(2026)).willReturn(apiResult);
        given(holidayMapper.mergeHoliday(any())).willReturn(1);

        int count = adminHolidayService.syncHolidays(2026);

        assertThat(count).isEqualTo(3);
        then(holidayMapper).should(times(3)).mergeHoliday(any());
    }

    @Test
    @DisplayName("API 동기화 성공 - 공휴일 없는 경우 0 반환")
    void syncHolidays_empty_success() {

        given(holidayApiClient.fetchHolidays(2026)).willReturn(List.of());

        int count = adminHolidayService.syncHolidays(2026);

        assertThat(count).isZero();
        then(holidayMapper).should(never()).mergeHoliday(any());
    }

    // ─────────────────────────────────────────────────────────────
    //  수동 등록
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수동 등록 성공")
    void createHoliday_success() {

        HolidayManualRequest request = createRequest("창립기념일", LocalDate.of(2026, 8, 15));
        HolidayVO vo = createHolidayVO(1L, "창립기념일", "MANUAL");

        given(holidayDtoMapper.toVO(request)).willReturn(vo);
        given(holidayMapper.insertHoliday(vo)).willReturn(1);

        Long result = adminHolidayService.createHoliday(request);

        assertThat(result).isEqualTo(1L);
        then(holidayMapper).should().insertHoliday(any());
    }

    // ─────────────────────────────────────────────────────────────
    //  수정
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수정 성공 - MANUAL 공휴일")
    void modifyHoliday_manual_success() {

        HolidayVO existing = createHolidayVO(1L, "창립기념일", "MANUAL");
        HolidayManualRequest request = createRequest("창립기념일 수정", LocalDate.of(2026, 8, 15));

        given(holidayMapper.selectHoliday(1L)).willReturn(existing);
        given(holidayMapper.updateHoliday(any())).willReturn(1);

        assertThatCode(() -> adminHolidayService.modifyHoliday(1L, request))
                .doesNotThrowAnyException();

        then(holidayMapper).should().updateHoliday(any());
    }

    @Test
    @DisplayName("수정 실패 - API 공휴일은 수정 불가")
    void modifyHoliday_api_fail() {

        HolidayVO existing = createHolidayVO(1L, "신정", "API");
        HolidayManualRequest request = createRequest("신정 수정", LocalDate.of(2026, 1, 1));

        given(holidayMapper.selectHoliday(1L)).willReturn(existing);

        assertThatThrownBy(() -> adminHolidayService.modifyHoliday(1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.HOLIDAY_API_MODIFY_DENIED.getMessage());
    }

    @Test
    @DisplayName("수정 실패 - 존재하지 않는 공휴일")
    void modifyHoliday_notFound_fail() {

        HolidayManualRequest request = createRequest("없는 공휴일", LocalDate.of(2026, 1, 1));
        given(holidayMapper.selectHoliday(999L)).willReturn(null);

        assertThatThrownBy(() -> adminHolidayService.modifyHoliday(999L, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.HOLIDAY_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────────────────────
    //  삭제
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("삭제 성공 - MANUAL 공휴일")
    void deleteHoliday_manual_success() {

        HolidayVO existing = createHolidayVO(1L, "창립기념일", "MANUAL");
        given(holidayMapper.selectHoliday(1L)).willReturn(existing);
        given(holidayMapper.deleteHoliday(1L)).willReturn(1);

        assertThatCode(() -> adminHolidayService.deleteHoliday(1L))
                .doesNotThrowAnyException();

        then(holidayMapper).should().deleteHoliday(1L);
    }

    @Test
    @DisplayName("삭제 실패 - API 공휴일은 삭제 불가")
    void deleteHoliday_api_fail() {

        HolidayVO existing = createHolidayVO(1L, "신정", "API");
        given(holidayMapper.selectHoliday(1L)).willReturn(existing);

        assertThatThrownBy(() -> adminHolidayService.deleteHoliday(1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.HOLIDAY_API_MODIFY_DENIED.getMessage());
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 공휴일")
    void deleteHoliday_notFound_fail() {

        given(holidayMapper.selectHoliday(999L)).willReturn(null);

        assertThatThrownBy(() -> adminHolidayService.deleteHoliday(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.HOLIDAY_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────────────────────
    //  헬퍼 메서드
    // ─────────────────────────────────────────────────────────────

    private HolidayVO createHolidayVO(Long holidayId, String holidayNm, String genTypeCd) {
        return HolidayVO.builder()
                .holidayId(holidayId)
                .holidayDt(LocalDate.of(2026, 1, 1))
                .holidayNm(holidayNm)
                .isHolidayYn("Y")
                .genTypeCd(genTypeCd)
                .syncDt(LocalDateTime.now())
                .build();
    }

    private HolidayResponse createHolidayResponse(Long holidayId, String holidayNm, String genTypeCd) {
        return HolidayResponse.builder()
                .holidayId(holidayId)
                .holidayDt(LocalDate.of(2026, 1, 1))
                .holidayNm(holidayNm)
                .isHolidayYn("Y")
                .genTypeCd(genTypeCd)
                .build();
    }

    private HolidayManualRequest createRequest(String holidayNm, LocalDate holidayDt) {
        HolidayManualRequest request = new HolidayManualRequest();
        try {
            var f1 = HolidayManualRequest.class.getDeclaredField("holidayNm");
            f1.setAccessible(true); f1.set(request, holidayNm);
            var f2 = HolidayManualRequest.class.getDeclaredField("holidayDt");
            f2.setAccessible(true); f2.set(request, holidayDt);
            var f3 = HolidayManualRequest.class.getDeclaredField("isHolidayYn");
            f3.setAccessible(true); f3.set(request, "Y");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }
}