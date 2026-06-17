package com.mycrewsoft.domain.mtng.mapper;

import com.mycrewsoft.domain.mtng.dto.response.AdminMtngAnalyticsItemResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngListResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngPtcptResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngStatsResponse;
import com.mycrewsoft.domain.mtng.vo.AdminMtngPtcptVO;
import com.mycrewsoft.domain.mtng.vo.AdminMtngStatsVO;
import com.mycrewsoft.domain.mtng.vo.MtngAnalyticsVO;
import com.mycrewsoft.domain.mtng.vo.MtngListVO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminMtngDtoMapper {

    AdminMtngListResponse toListResponse(MtngListVO vo);

    @Mapping(target = "ptcpts", ignore = true)
    AdminMtngDetailResponse toDetailResponse(MtngListVO vo);

    List<AdminMtngListResponse> toListResponseList(List<MtngListVO> voList);

    AdminMtngPtcptResponse toPtcptResponse(AdminMtngPtcptVO vo);

    List<AdminMtngPtcptResponse> toPtcptResponseList(List<AdminMtngPtcptVO> voList);

    AdminMtngStatsResponse toStatsResponse(AdminMtngStatsVO vo);

    @Mapping(source = "category", target = "label")
    AdminMtngAnalyticsItemResponse toAnalyticsItemResponse(MtngAnalyticsVO vo);

    @Mapping(source = "category", target = "label")
    List<AdminMtngAnalyticsItemResponse> toAnalyticsItemResponseList(List<MtngAnalyticsVO> voList);
}