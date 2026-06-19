package com.mycrewsoft.domain.dashboard.service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.dashboard.dto.request.DashboardLayoutRequest;
import com.mycrewsoft.domain.dashboard.dto.response.DashboardLayoutResponse;
import com.mycrewsoft.domain.dashboard.mapper.DashboardDtoMapper;
import com.mycrewsoft.domain.dashboard.mapper.DashboardLayoutMapper;
import com.mycrewsoft.domain.dashboard.vo.DashboardLayoutVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final DashboardLayoutMapper       dashboardLayoutMapper;
    private final DashboardDtoMapper          dashboardDtoMapper;

    @Override
    public DashboardLayoutResponse readDashboardLayout() {
        Long empId = getCurrentEmpIdOrThrow();

        DashboardLayoutVO vo = dashboardLayoutMapper.selectDashboardLayout(empId);

        if (vo == null) {
            return DashboardLayoutResponse.builder()
                    .empId(empId)
                    .lytJsonCn(getDefaultLayout())
                    .build();
        }

        return dashboardDtoMapper.toResponse(vo);
    }

    @Override
    @Transactional
    public void saveDashboardLayout(DashboardLayoutRequest request) {
        Long empId = getCurrentEmpIdOrThrow();

        DashboardLayoutVO existing = dashboardLayoutMapper.selectDashboardLayout(empId);

        DashboardLayoutVO vo = new DashboardLayoutVO();
        vo.setEmpId(empId);
        vo.setLytJsonCn(request.getLytJsonCn());

        if (existing == null) {
            dashboardLayoutMapper.insertDashboardLayout(vo);
        } else {
            dashboardLayoutMapper.updateDashboardLayout(vo);
        }
    }
    
    private Long getCurrentEmpIdOrThrow() {
        Long empId = SecurityUtil.getCurrentEmpId();
        if (empId == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return empId;
    }
    
    // 디폴트 레이아웃
    private String getDefaultLayout() {
        return """
                {
                  "widgets": [
                    { "key": "attendance",      "x": 0, "y": 0, "w": 4, "h": 3 },
                    { "key": "approval",        "x": 4, "y": 0, "w": 4, "h": 3 },
                    { "key": "projectProgress", "x": 8, "y": 0, "w": 4, "h": 3 },
                    { "key": "todaySchedule",   "x": 0, "y": 3, "w": 4, "h": 3 },
                    { "key": "task",            "x": 4, "y": 3, "w": 4, "h": 3 },
                    { "key": "board",          "x": 8, "y": 3, "w": 4, "h": 3 }
                  ]
                }
                """;
    }
}