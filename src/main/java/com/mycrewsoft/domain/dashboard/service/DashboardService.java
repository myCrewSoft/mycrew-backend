package com.mycrewsoft.domain.dashboard.service;

import com.mycrewsoft.domain.dashboard.dto.request.DashboardLayoutRequest;
import com.mycrewsoft.domain.dashboard.dto.response.DashboardLayoutResponse;

public interface DashboardService {

    DashboardLayoutResponse readDashboardLayout();

    void saveDashboardLayout(DashboardLayoutRequest request);
}