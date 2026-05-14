package com.mycrewsoft.app.domain.notification.mapper;

import com.mycrewsoft.app.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.app.domain.notification.vo.NotificationVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

/**
 * componentModel = "spring" → Spring 빈으로 자동 등록되어 @Autowired 사용 가능.
 * 구현체는 컴파일 시 자동 생성된다. 직접 구현 금지.
 *
 * 팀원 가이드: 도메인별로 이 패턴대로 Mapper 인터페이스를 추가하면 된다.
 * 예) BoardDtoMapper, CommentDtoMapper ...
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    /** 필드명이 같으면 자동 매핑된다. */
    NotificationResponse toResponseDto(NotificationVo vo);

    /** 필드명이 다르면 @Mapping 으로 명시한다. */
    @Mapping(source = "createdAt", target = "registeredDate")
    NotificationResponse toResponseDtoWithMapping(NotificationVo vo);

    /** 목록 변환. List<UserVo> → List<UserResponseDto> */
    List<NotificationResponse> toResponseDtoList(List<NotificationVo> voList);
}