package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.domain.mtng.dto.request.AdminMtngListRequest;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngListPageResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngStatsResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngAnalyticsResponse;

public interface AdminMtngService {

    AdminMtngListPageResponse getAdminMtngList(AdminMtngListRequest request);

    AdminMtngDetailResponse getAdminMtngDetail(Long mtngId);

    AdminMtngStatsResponse getAdminMtngStats();

    AdminMtngAnalyticsResponse getAdminMtngAnalytics();

    void forceEndMtng(Long mtngId);
}