package com.mycrewsoft.domain.holiday.client;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.holiday.vo.HolidayApiItemVO;
import com.mycrewsoft.domain.holiday.vo.HolidayVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayApiClient {

    private final RestTemplate restTemplate;

    @Value("${holiday.api.key}")
    private String apiKey;

    @Value("${holiday.api.base-url}")
    private String baseUrl;

    private static final String ENDPOINT = "/getRestDeInfo";
    private static final int NUM_OF_ROWS = 100;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<HolidayVO> fetchHolidays(int year) {
        List<HolidayVO> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            try {
                List<HolidayVO> monthly = fetchByMonth(year, month);
                result.addAll(monthly);
            } catch (CustomException e) {
                throw e;
            } catch (Exception e) {
                log.error("공휴일 API 호출 실패 year={}, month={}", year, month, e);
                throw new CustomException(ErrorCode.HOLIDAY_API_CALL_FAILED);
            }
        }

        return result;
    }

    private List<HolidayVO> fetchByMonth(int year, int month) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + ENDPOINT)
                .queryParam("serviceKey", apiKey)
                .queryParam("solYear", year)
                .queryParam("solMonth", String.format("%02d", month))
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam("pageNo", 1)
                .queryParam("_type", "json")
                .build(true)
                .toUriString();

        KasiHolidayApiResponse response = restTemplate.getForObject(url, KasiHolidayApiResponse.class);

        if (response == null
                || response.getResponse() == null
                || response.getResponse().getBody() == null) {
            log.warn("공휴일 API 응답 없음 year={}, month={}", year, month);
            return List.of();
        }

        KasiHolidayApiResponse.Body body = response.getResponse().getBody();

        if (body.getItems() == null || body.getItems().getItem() == null) {
            return List.of();
        }

        return body.getItems().getItem().stream()
                .map(this::toVO)
                .toList();
    }

    private HolidayVO toVO(HolidayApiItemVO item) {
        return HolidayVO.builder()
                .holidayDt(LocalDate.parse(item.getLocdate(), DATE_FORMATTER))
                .holidayNm(item.getDateName())
                .isHolidayYn(item.getIsHoliday())
                .genTypeCd("API")
                .build();
    }
}