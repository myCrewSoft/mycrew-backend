package com.mycrewsoft.domain.mtng.mapper;

import com.mycrewsoft.domain.mtng.dto.request.AdminMtngListRequest;
import com.mycrewsoft.domain.mtng.vo.AdminMtngPtcptVO;
import com.mycrewsoft.domain.mtng.vo.AdminMtngStatsVO;
import com.mycrewsoft.domain.mtng.vo.MtngAnalyticsVO;
import com.mycrewsoft.domain.mtng.vo.MtngListVO;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMtngMapper {

    List<MtngListVO> selectAdminMtngList(AdminMtngListRequest request);

    int selectAdminMtngTotalCount(AdminMtngListRequest request);

    MtngListVO selectAdminMtngDetail(Long mtngId);

    List<AdminMtngPtcptVO> selectAdminMtngPtcptList(Long mtngId);

    AdminMtngStatsVO selectAdminMtngStats();

    List<MtngAnalyticsVO> selectMtngTypeStats();

    List<MtngAnalyticsVO> selectMtngMonthlyStats();

    List<MtngAnalyticsVO> selectMtngHourlyStats();

    int selectMomGenerationRate();

    int updateMtngForceEnd(Long mtngId);
}