package com.mycrewsoft.domain.room.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycrewsoft.domain.room.dto.request.RoomCreateRequest;
import com.mycrewsoft.domain.room.dto.request.RoomUpdateRequest;
import com.mycrewsoft.domain.room.dto.response.ConfRmListItem;
import com.mycrewsoft.domain.room.dto.response.ConfRmStatsSummary;
import com.mycrewsoft.domain.room.dto.response.RoomResponse;
import com.mycrewsoft.domain.room.vo.ConfRmAdminVO;
import com.mycrewsoft.domain.room.vo.ConfRmSummaryVO;
import com.mycrewsoft.domain.room.vo.ConfRmVO;

@Mapper(componentModel = "spring")
public interface RoomDtoMapper {

    ConfRmVO toVO(RoomCreateRequest request);

    ConfRmVO toVO(RoomUpdateRequest request);

    @Mapping(source = "confRmId", target = "roomId")
    @Mapping(source = "confRmNm", target = "roomName")
    @Mapping(source = "confRmHo", target = "ho")
    @Mapping(source = "confRmFlr", target = "floor")
    RoomResponse toResponse(ConfRmVO vo);
    
    List<RoomResponse> toResponseList(List<ConfRmVO> voList);

    ConfRmStatsSummary toConfRmStatsSummary(ConfRmSummaryVO vo);

    ConfRmListItem toConfRmListItem(ConfRmAdminVO vo);
}
