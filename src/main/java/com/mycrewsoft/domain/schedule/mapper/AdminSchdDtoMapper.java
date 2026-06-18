package com.mycrewsoft.domain.schedule.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycrewsoft.domain.schedule.dto.request.AdminSchdRequest;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdListResponse;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdResponse;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleTargetResponseDto;
import com.mycrewsoft.domain.schedule.vo.AdminSchdListVO;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetDetailVO;

@Mapper(componentModel = "spring")
public interface AdminSchdDtoMapper {

    @Mapping(target = "schdId", ignore = true)
    @Mapping(target = "schdWrtrId", source = "empId")
    @Mapping(target = "schdRegstDt", ignore = true)
    @Mapping(target = "schdChgrId", ignore = true)
    @Mapping(target = "schdChgDt", ignore = true)
    @Mapping(target = "delYn", ignore = true)
    @Mapping(target = "delDt", ignore = true)
    @Mapping(target = "targets", ignore = true)
    IntgSchdVO toVO(AdminSchdRequest dto, Long empId);

    AdminSchdListResponse toListResponse(AdminSchdListVO vo);

    List<AdminSchdListResponse> toListResponseList(List<AdminSchdListVO> voList);

    @Mapping(target = "targets", source = "targets")
    AdminSchdResponse toResponse(IntgSchdVO vo, List<SchdTargetDetailVO> targets);

    @Mapping(target = "targetNm", source = "targetNm")
    @Mapping(target = "deptNm", source = "deptNm")
    @Mapping(target = "jobGrdNm", source = "jobGrdNm")
    @Mapping(target = "profileImgUrl", source = "profileImgUrl")
    ScheduleTargetResponseDto toTargetResponse(SchdTargetDetailVO vo);
}