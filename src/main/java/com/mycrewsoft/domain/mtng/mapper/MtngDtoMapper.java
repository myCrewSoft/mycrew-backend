package com.mycrewsoft.domain.mtng.mapper;

import com.mycrewsoft.domain.mtng.dto.response.MtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.MtngListResponse;
import com.mycrewsoft.domain.mtng.dto.response.MtngPtcptResponse;
import com.mycrewsoft.domain.mtng.enums.MtngSttus;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngListVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MtngDtoMapper {

    @Mapping(target = "mtngSttus", expression = "java(calcSttus(vo, now).getValue())")
    MtngListResponse toMtngListResponse(MtngListVO vo, LocalDateTime now);

    default MtngSttus calcSttus(MtngListVO vo, LocalDateTime now) {
        return MtngSttus.of(vo.getBeginDt(), vo.getEndDt(), now);
    }

    MtngPtcptResponse toPtcptResponse(MtngPtcptDetailVO vo);

    List<MtngPtcptResponse> toPtcptResponseList(List<MtngPtcptDetailVO> voList);

    @Mapping(target = "mtngSttus", expression = "java(calcSttus(vo, now).getValue())")
    MtngDetailResponse toMtngDetailResponse(
            MtngDetailVO vo,
            List<MtngPtcptResponse> ptcptList,
            LocalDateTime now
    );

    default MtngSttus calcSttus(MtngDetailVO vo, LocalDateTime now) {
        return MtngSttus.of(vo.getBeginDt(), vo.getEndDt(), now);
    }
}