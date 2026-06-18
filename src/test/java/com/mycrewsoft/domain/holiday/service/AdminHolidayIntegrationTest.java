package com.mycrewsoft.domain.holiday.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.domain.holiday.client.HolidayApiClient;
import com.mycrewsoft.domain.holiday.dto.response.HolidayResponse;
import com.mycrewsoft.domain.holiday.mapper.HolidayMapper;
import com.mycrewsoft.domain.holiday.vo.HolidayVO;

@SpringBootTest
@Transactional
@DisplayName("관리자 공휴일 통합 테스트")
class AdminHolidayIntegrationTest {

    @Autowired
    private AdminHolidayService adminHolidayService;

    @Autowired
    private HolidayMapper holidayMapper;

    @MockBean
    private HolidayApiClient holidayApiClient;

    @Test
    @DisplayName("수동 등록 후 단건 조회 성공")
    void createAndGetHoliday_success() {

        HolidayVO vo = HolidayVO.builder()
                .holidayDt(LocalDate.of(2026, 8, 15))
                .holidayNm("창립기념일")
                .isHolidayYn("Y")
                .genTypeCd("MANUAL")
                .build();
        holidayMapper.insertHoliday(vo);

        HolidayResponse result = adminHolidayService.getHoliday(vo.getHolidayId());

        assertThat(result).isNotNull();
        assertThat(result.getHolidayNm()).isEqualTo("창립기념일");
        assertThat(result.getGenTypeCd()).isEqualTo("MANUAL");
    }

    @Test
    @DisplayName("API 동기화 후 목록 조회 - MERGE 중복 방지 확인")
    void syncAndList_mergeDedup_success() {

        List<HolidayVO> apiResult = List.of(
                HolidayVO.builder()
                        .holidayDt(LocalDate.of(2026, 1, 1))
                        .holidayNm("신정")
                        .isHolidayYn("Y")
                        .genTypeCd("API")
                        .build()
        );

        given(holidayApiClient.fetchHolidays(2026)).willReturn(apiResult);

        adminHolidayService.syncHolidays(2026);
        adminHolidayService.syncHolidays(2026);

        List<HolidayResponse> result = adminHolidayService.getHolidayList(2026);

        long count = result.stream()
                .filter(r -> r.getHolidayDt().equals(LocalDate.of(2026, 1, 1)))
                .count();

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("논리 삭제 후 조회 - 예외 발생 확인")
    void deleteAndGet_notFound() {

        HolidayVO vo = HolidayVO.builder()
                .holidayDt(LocalDate.of(2026, 5, 5))
                .holidayNm("어린이날")
                .isHolidayYn("Y")
                .genTypeCd("MANUAL")
                .build();
        holidayMapper.insertHoliday(vo);

        adminHolidayService.deleteHoliday(vo.getHolidayId());

        assertThatThrownBy(() -> adminHolidayService.getHoliday(vo.getHolidayId()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("API 공휴일 삭제 시도 - 예외 발생 확인")
    void deleteApiHoliday_fail() {

        HolidayVO vo = HolidayVO.builder()
                .holidayDt(LocalDate.of(2026, 1, 1))
                .holidayNm("신정")
                .isHolidayYn("Y")
                .genTypeCd("API")
                .build();
        holidayMapper.insertHoliday(vo);

        assertThatThrownBy(() -> adminHolidayService.deleteHoliday(vo.getHolidayId()))
                .isInstanceOf(CustomException.class);
    }
}