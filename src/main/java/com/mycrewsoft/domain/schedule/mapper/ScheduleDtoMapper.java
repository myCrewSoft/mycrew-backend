package com.mycrewsoft.domain.schedule.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.mycrewsoft.domain.schedule.dto.request.ScheduleRequestDto;
import com.mycrewsoft.domain.schedule.dto.request.ScheduleTargetRequestDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleResponseDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleTargetResponseDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleWidgetItemResponse;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetDetailVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetVO;
import com.mycrewsoft.domain.schedule.vo.SchdWidgetVO;

@Mapper(componentModel = "spring")
public interface ScheduleDtoMapper {

	/**
	 * ScheduleRequestDto -> IntgSchdVO로 변환
	 * @param ScheduleRequestDto
	 * @return IntgSchdVO
	 */
	@Mapping(target = "schdId", ignore = true)
	@Mapping(target = "schdWrtrId", source = "empId")
	@Mapping(target = "schdRegstDt", ignore = true)
	@Mapping(target = "schdChgrId", ignore = true)
	@Mapping(target = "schdChgDt", ignore = true)
	@Mapping(target = "delYn", ignore = true)
	@Mapping(target = "delDt", ignore = true)
	IntgSchdVO toVo(ScheduleRequestDto dto, Long empId);
	
	/**
	 * IntgSchdVo -> SchdResponseDto 변환.
	 * @param IntgSchdVO
	 * @return ScheduleResponseDto
	 */
	@Mapping(target = "id", source = "schdId")
	@Mapping(target = "scheduleTypeCode", source = "schdClsfCd")
	@Mapping(target = "title", source = "schdNm")
	@Mapping(target = "detail", source = "schdDetailCn")
	@Mapping(target = "start", source = "beginDt")
	@Mapping(target = "end", source = "endDt")
	@Mapping(target = "allDay", source = "allDayYn", qualifiedByName = "ynToBoolean")
	@Mapping(target = "repeat", source = "reptYn", qualifiedByName = "ynToBoolean")
	@Mapping(target = "repeatTypeCode", source = "reptTypeCd")
	@Mapping(target = "repeatEndDate", source = "reptEndDt")
	ScheduleResponseDto toResponseDto(IntgSchdVO vo);

	@Mapping(target = "id",               source = "vo.schdId")
	@Mapping(target = "scheduleTypeCode",  source = "vo.schdClsfCd")
	@Mapping(target = "title",             source = "vo.schdNm")
	@Mapping(target = "detail",            source = "vo.schdDetailCn")
	@Mapping(target = "start",             source = "vo.beginDt")
	@Mapping(target = "end",               source = "vo.endDt")
	@Mapping(target = "allDay",            source = "vo.allDayYn",  qualifiedByName = "ynToBoolean")
	@Mapping(target = "repeat",            source = "vo.reptYn",    qualifiedByName = "ynToBoolean")
	@Mapping(target = "repeatTypeCode",    source = "vo.reptTypeCd")
	@Mapping(target = "repeatEndDate",     source = "vo.reptEndDt")
	@Mapping(target = "targets",           source = "targets")
	@Mapping(target = "deptCd",            ignore = true)
	@Mapping(target = "projId",            ignore = true)
	@Mapping(target = "taskId",            ignore = true)
	@Mapping(target = "writerId",          ignore = true)
	@Mapping(target = "writerName",        ignore = true)
	ScheduleResponseDto toResponseDto(IntgSchdVO vo, List<SchdTargetDetailVO> targets);

	ScheduleTargetResponseDto toTargetResponseDto(SchdTargetDetailVO vo);

	List<ScheduleTargetResponseDto> toTargetResponseDtoList(List<SchdTargetDetailVO> voList);
	
	/**
	 * IntgSchdVo 리스트 -> SchdResponseDto 리스트 변환.
	 * @param voList
	 * @return List<ScheduleResponseDto>
	 */
	List<ScheduleResponseDto> toDtoList(List<IntgSchdVO> voList);

    @Mapping(target = "schdId", source = "schdId")
    SchdTargetVO toTargetVo(ScheduleTargetRequestDto dto, Long schdId);

    @Mapping(target = "id",               source = "id")
    @Mapping(target = "scheduleTypeCode", source = "scheduleTypeCode")
    @Mapping(target = "title",            source = "title")
    @Mapping(target = "start",            source = "startDt")
    @Mapping(target = "end",              source = "endDt")
    @Mapping(target = "allDay",           source = "allDayYn", qualifiedByName = "ynToBoolean")
    @Mapping(target = "deptNm",           source = "deptNm")
    @Mapping(target = "projNm",           source = "projNm")
    @Mapping(target = "taskNm",           source = "taskNm")
    ScheduleWidgetItemResponse toWidgetItemResponse(SchdWidgetVO vo);

    List<ScheduleWidgetItemResponse> toWidgetItemResponseList(List<SchdWidgetVO> voList);
    
    default List<SchdTargetVO> toTargetVoList(List<ScheduleTargetRequestDto> dtoList, Long schdId) {
        if (dtoList == null) return List.of();
        return dtoList.stream()
                .map(dto -> toTargetVo(dto, schdId))
                .toList();
    }
    
    // String -> Boolean
    @Named("ynToBoolean")
    default Boolean ynToBoolean(String value) {
        if (value == null) return false;
        return "Y".equalsIgnoreCase(value);
    }
}
