package com.mycrewsoft.domain.dashboard.mapper;

import com.mycrewsoft.domain.dashboard.vo.DashboardLayoutVO;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashboardLayoutMapper {

    DashboardLayoutVO selectDashboardLayout(Long empId);

    int insertDashboardLayout(DashboardLayoutVO vo);

    int updateDashboardLayout(DashboardLayoutVO vo);
}