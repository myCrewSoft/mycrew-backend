package com.mycrewsoft.domain.holiday.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.holiday.vo.HolidayVO;

@Mapper
public interface HolidayMapper {

    List<HolidayVO> selectHolidayList(@Param("year") int year);

    HolidayVO selectHoliday(@Param("holidayId") Long holidayId);

    int mergeHoliday(HolidayVO vo);

    int insertHoliday(HolidayVO vo);

    int updateHoliday(HolidayVO vo);

    int deleteHoliday(@Param("holidayId") Long holidayId);
}