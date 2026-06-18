package com.mycrewsoft.domain.holiday.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.holiday.client.HolidayApiClient;
import com.mycrewsoft.domain.holiday.dto.request.HolidayManualRequest;
import com.mycrewsoft.domain.holiday.dto.response.HolidayResponse;
import com.mycrewsoft.domain.holiday.mapper.HolidayDtoMapper;
import com.mycrewsoft.domain.holiday.mapper.HolidayMapper;
import com.mycrewsoft.domain.holiday.vo.HolidayVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminHolidayServiceImpl implements AdminHolidayService {

    private final HolidayMapper holidayMapper;
    private final HolidayApiClient holidayApiClient;
    private final HolidayDtoMapper holidayDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidayList(int year) {
        List<HolidayVO> voList = holidayMapper.selectHolidayList(year);
        return holidayDtoMapper.toResponseList(voList);
    }

    @Override
    @Transactional(readOnly = true)
    public HolidayResponse getHoliday(Long holidayId) {
        HolidayVO vo = holidayMapper.selectHoliday(holidayId);
        if (vo == null) throw new CustomException(ErrorCode.HOLIDAY_NOT_FOUND);
        return holidayDtoMapper.toResponse(vo);
    }

    @Override
    @Transactional
    public int syncHolidays(int year) {
        List<HolidayVO> holidays = holidayApiClient.fetchHolidays(year);

        for (HolidayVO vo : holidays) {
            holidayMapper.mergeHoliday(vo);
        }

        return holidays.size();
    }

    @Override
    @Transactional
    public Long createHoliday(HolidayManualRequest request) {
        HolidayVO vo = holidayDtoMapper.toVO(request);
        holidayMapper.insertHoliday(vo);
        return vo.getHolidayId();
    }

    @Override
    @Transactional
    public void modifyHoliday(Long holidayId, HolidayManualRequest request) {
        HolidayVO existing = holidayMapper.selectHoliday(holidayId);
        if (existing == null) throw new CustomException(ErrorCode.HOLIDAY_NOT_FOUND);
        if ("API".equals(existing.getGenTypeCd())) {
            throw new CustomException(ErrorCode.HOLIDAY_API_MODIFY_DENIED);
        }

        HolidayVO updateVO = HolidayVO.builder()
                .holidayId(holidayId)
                .holidayDt(request.getHolidayDt())
                .holidayNm(request.getHolidayNm())
                .isHolidayYn(request.getIsHolidayYn())
                .build();
        
        holidayMapper.updateHoliday(updateVO);
    }

    @Override
    @Transactional
    public void deleteHoliday(Long holidayId) {
        HolidayVO existing = holidayMapper.selectHoliday(holidayId);
        if (existing == null) throw new CustomException(ErrorCode.HOLIDAY_NOT_FOUND);
        if ("API".equals(existing.getGenTypeCd())) {
            throw new CustomException(ErrorCode.HOLIDAY_API_MODIFY_DENIED);
        }

        holidayMapper.deleteHoliday(holidayId);
    }
}