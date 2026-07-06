package com.mycrewsoft.domain.dashboard.mapper;

import com.mycrewsoft.domain.dashboard.dto.response.DashboardLayoutResponse;
import com.mycrewsoft.domain.dashboard.vo.DashboardLayoutVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DashboardDtoMapper {

    DashboardLayoutResponse toResponse(DashboardLayoutVO vo);
}