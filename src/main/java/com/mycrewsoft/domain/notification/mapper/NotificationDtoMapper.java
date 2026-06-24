package com.mycrewsoft.domain.notification.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.domain.notification.vo.AlrmRcvrVO;
import com.mycrewsoft.domain.notification.vo.AlrmVO;
import com.mycrewsoft.domain.notification.vo.NotificationQueryVO;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

    NotificationResponse toResponse(NotificationQueryVO vo);

    List<NotificationResponse> toResponseList(List<NotificationQueryVO> voList);

    @Mapping(target = "alrmRcvrId", source = "alrmRcvrVO.alrmRcvrId")
    @Mapping(target = "alrmId", source = "alrmVO.alrmId")
    @Mapping(target = "alrmTypeCd", source = "alrmVO.alrmTypeCd")
    @Mapping(target = "alrmTtln", source = "alrmVO.alrmTtln")
    @Mapping(target = "alrmCn", source = "alrmVO.alrmCn")
    @Mapping(target = "targetType", source = "alrmVO.targetType")
    @Mapping(target = "targetId", source = "alrmVO.targetId")
    @Mapping(target = "parentTargetId", source = "alrmVO.parentTargetId")
    @Mapping(target = "alrmSndngDt", source = "alrmVO.alrmSndngDt")
    @Mapping(target = "alrmCfmtnDt", source = "alrmRcvrVO.alrmCfmtnDt")
    NotificationResponse toResponse(AlrmVO alrmVO, AlrmRcvrVO alrmRcvrVO);
}
