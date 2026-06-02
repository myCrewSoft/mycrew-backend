package com.mycrewsoft.domain.notification.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.domain.notification.vo.AlrmVO;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

    NotificationResponse toResponse(AlrmVO vo);

    List<NotificationResponse> toResponseList(List<AlrmVO> voList);
}
