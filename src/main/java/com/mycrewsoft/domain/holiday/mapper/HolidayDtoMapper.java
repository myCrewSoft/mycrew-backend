package com.mycrewsoft.domain.holiday.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycrewsoft.domain.holiday.dto.request.HolidayManualRequest;
import com.mycrewsoft.domain.holiday.dto.response.HolidayResponse;
import com.mycrewsoft.domain.holiday.vo.HolidayVO;

@Mapper(componentModel = "spring")
public interface HolidayDtoMapper {

    HolidayResponse toResponse(HolidayVO vo);

    List<HolidayResponse> toResponseList(List<HolidayVO> voList);

    @Mapping(target = "holidayId", ignore = true)
    @Mapping(target = "genTypeCd", constant = "MANUAL")
    @Mapping(target = "syncDt", ignore = true)
    @Mapping(target = "delYn", ignore = true)
    @Mapping(target = "frstRegDt", ignore = true)
    HolidayVO toVO(HolidayManualRequest request);
}