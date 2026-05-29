package com.mycrewsoft.domain.schedule.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycrewsoft.domain.schedule.dto.request.ScheduleRequestDto;
import com.mycrewsoft.domain.schedule.dto.request.ScheduleTargetRequestDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleResponseDto;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetVO;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

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
	ScheduleResponseDto toResponseDto(IntgSchdVO vo);
	
	/**
	 * IntgSchdVo 리스트 -> SchdResponseDto 리스트 변환.
	 * @param voList
	 * @return List<ScheduleResponseDto>
	 */
	List<ScheduleResponseDto> toDtoList(List<IntgSchdVO> voList);

    @Mapping(target = "schdId", source = "schdId")
    SchdTargetVO toTargetVo(ScheduleTargetRequestDto dto, Long schdId);

    default List<SchdTargetVO> toTargetVoList(List<ScheduleTargetRequestDto> dtoList, Long schdId) {
        if (dtoList == null) return List.of();
        return dtoList.stream()
                .map(dto -> toTargetVo(dto, schdId))
                .toList();
    }
}
