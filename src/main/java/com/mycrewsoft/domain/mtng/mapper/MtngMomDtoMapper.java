package com.mycrewsoft.domain.mtng.mapper;

import com.mycrewsoft.domain.mtng.dto.mom.response.MtngMomHistResponse;
import com.mycrewsoft.domain.mtng.dto.mom.response.MtngMomResponse;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomHistVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomVO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MtngMomDtoMapper {

    @Mapping(target = "histList", source = "mtngMomHist")
    MtngMomResponse toMomResponse(MtngMomVO vo);

    MtngMomHistResponse toMomHistResponse(MtngMomHistVO vo);

    List<MtngMomHistResponse> toMomHistResponseList(List<MtngMomHistVO> voList);
}