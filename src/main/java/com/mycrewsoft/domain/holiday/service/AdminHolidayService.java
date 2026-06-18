package com.mycrewsoft.domain.holiday.service;

import java.util.List;

import com.mycrewsoft.domain.holiday.dto.request.HolidayManualRequest;
import com.mycrewsoft.domain.holiday.dto.response.HolidayResponse;

public interface AdminHolidayService {

    List<HolidayResponse> getHolidayList(int year);

    HolidayResponse getHoliday(Long holidayId);

    int syncHolidays(int year);

    Long createHoliday(HolidayManualRequest request);

    void modifyHoliday(Long holidayId, HolidayManualRequest request);

    void deleteHoliday(Long holidayId);
}