package com.mycrewsoft.domain.holiday.client;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Value("${holiday.api.key}")
    private String apiKey;

    @Value("${holiday.api.base-url}")
    private String baseUrl;

    private static final String ENDPOINT_REST      = "/getRestDeInfo";
    private static final String ENDPOINT_HOLI      = "/getHoliDeInfo";
    private static final String ENDPOINT_ANNIVERSARY = "/getAnniversaryInfo";
    private static final int NUM_OF_ROWS = 100;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<HolidayVO> fetchHolidays(int year) {
        List<HolidayVO> result = new ArrayList<>();

        for (String endpoint : List.of(ENDPOINT_REST, ENDPOINT_HOLI, ENDPOINT_ANNIVERSARY)) {
            for (int month = 1; month <= 12; month++) {
                try {
                    result.addAll(fetchByMonthAndEndpoint(endpoint, year, month));
                } catch (CustomException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("공휴일 API 호출 실패 endpoint={}, year={}, month={}", endpoint, year, month, e);
                    throw new CustomException(ErrorCode.HOLIDAY_API_CALL_FAILED);
                }
            }
        }

        return result;
    }

    private List<HolidayVO> fetchByMonthAndEndpoint(String endpoint, int year, int month) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint)
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
            log.warn("공휴일 API 응답 없음 endpoint={}, year={}, month={}", endpoint, year, month);
            return List.of();
        }

        KasiHolidayApiResponse.Body body = response.getResponse().getBody();

        if (body == null || body.getTotalCount() == 0) {
            return List.of();
        }

        JsonNode itemsNode = body.getItems();

        if (itemsNode == null || itemsNode.isNull() || !itemsNode.isObject()) {
            return List.of();
        }

        JsonNode itemNode = itemsNode.get("item");

        if (itemNode == null || itemNode.isNull()) {
            return List.of();
        }

        List<HolidayApiItemVO> items = new ArrayList<>();

        if (itemNode.isArray()) {
            for (JsonNode node : itemNode) {
                try {
                    items.add(objectMapper.treeToValue(node, HolidayApiItemVO.class));
                } catch (JsonProcessingException e) {
                    log.error("공휴일 item 파싱 실패: {}", node, e);
                    throw new CustomException(ErrorCode.HOLIDAY_API_CALL_FAILED);
                }
            }
        } else if (itemNode.isObject()) {
            try {
                items.add(objectMapper.treeToValue(itemNode, HolidayApiItemVO.class));
            } catch (JsonProcessingException e) {
                log.error("공휴일 item 파싱 실패: {}", itemNode, e);
                throw new CustomException(ErrorCode.HOLIDAY_API_CALL_FAILED);
            }
        }

        return items.stream()
                .map(item -> toVO(item, endpoint))
                .toList();
    }

    private HolidayVO toVO(HolidayApiItemVO item, String endpoint) {
        String isHolidayYn = ENDPOINT_ANNIVERSARY.equals(endpoint) ? "N" : item.getIsHoliday();
        return HolidayVO.builder()
                .holidayDt(LocalDate.parse(item.getLocdate(), DATE_FORMATTER))
                .holidayNm(item.getDateName())
                .isHolidayYn(isHolidayYn)
                .genTypeCd("API")
                .build();
    }
}