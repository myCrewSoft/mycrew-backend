package com.mycrewsoft.domain.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.domain.dashboard.dto.request.DashboardLayoutRequest;
import com.mycrewsoft.domain.dashboard.dto.response.DashboardLayoutResponse;
import com.mycrewsoft.domain.dashboard.mapper.DashboardDtoMapper;
import com.mycrewsoft.domain.dashboard.mapper.DashboardLayoutMapper;
import com.mycrewsoft.domain.dashboard.vo.DashboardLayoutVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Mock
    private DashboardLayoutMapper dashboardLayoutMapper;

    @Mock
    private DashboardDtoMapper dashboardDtoMapper;

    private static final Long EMP_ID = 1L;
    private static final String JOB_RANK_CD = "RANK_GENERAL";
    private static final String DEFAULT_LAYOUT_JSON = "{\"widgets\":[]}";

    @Test
    @DisplayName("레이아웃 조회 - DB에 저장된 레이아웃이 있으면 저장된 레이아웃 반환")
    void readDashboardLayout_existingLayout() {
        DashboardLayoutVO vo = new DashboardLayoutVO();
        vo.setDshbdLytId(1L);
        vo.setEmpId(EMP_ID);
        vo.setLytJsonCn("{\"widgets\":[]}");

        DashboardLayoutResponse expected = DashboardLayoutResponse.builder()
                .dshbdLytId(1L)
                .empId(EMP_ID)
                .lytJsonCn("{\"widgets\":[]}")
                .build();

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(EMP_ID);

            given(dashboardLayoutMapper.selectDashboardLayout(EMP_ID)).willReturn(vo);
            given(dashboardDtoMapper.toResponse(vo)).willReturn(expected);

            DashboardLayoutResponse result = dashboardService.readDashboardLayout();

            assertThat(result.getLytJsonCn()).isEqualTo("{\"widgets\":[]}");
            assertThat(result.getEmpId()).isEqualTo(EMP_ID);
        }
    }

    @Test
    @DisplayName("레이아웃 조회 - DB에 저장된 레이아웃이 없으면 디폴트 레이아웃 반환")
    void readDashboardLayout_defaultLayout() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(EMP_ID);

            given(dashboardLayoutMapper.selectDashboardLayout(EMP_ID)).willReturn(null);

            DashboardLayoutResponse result = dashboardService.readDashboardLayout();

            assertThat(result.getEmpId()).isEqualTo(EMP_ID);
            assertThat(result.getLytJsonCn()).contains("attendance");
            assertThat(result.getLytJsonCn()).contains("approval");
        }
    }

    @Test
    @DisplayName("레이아웃 조회 - 인증 정보 없으면 예외 발생")
    void readDashboardLayout_unauthorized() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(null);

            assertThatThrownBy(() -> dashboardService.readDashboardLayout())
                    .isInstanceOf(CustomException.class);
        }
    }

    @Test
    @DisplayName("레이아웃 저장 - 기존 레이아웃 없으면 insert")
    void saveDashboardLayout_insert() {
        DashboardLayoutRequest request = new DashboardLayoutRequest();
        setField(request, "lytJsonCn", "{\"widgets\":[]}");

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(EMP_ID);

            given(dashboardLayoutMapper.selectDashboardLayout(EMP_ID)).willReturn(null);
            given(dashboardLayoutMapper.insertDashboardLayout(org.mockito.ArgumentMatchers.any())).willReturn(1);

            dashboardService.saveDashboardLayout(request);

            verify(dashboardLayoutMapper).insertDashboardLayout(org.mockito.ArgumentMatchers.any());
        }
    }

    @Test
    @DisplayName("레이아웃 저장 - 기존 레이아웃 있으면 update")
    void saveDashboardLayout_update() {
        DashboardLayoutRequest request = new DashboardLayoutRequest();
        setField(request, "lytJsonCn", "{\"widgets\":[]}");

        DashboardLayoutVO existing = new DashboardLayoutVO();
        existing.setEmpId(EMP_ID);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(EMP_ID);

            given(dashboardLayoutMapper.selectDashboardLayout(EMP_ID)).willReturn(existing);
            given(dashboardLayoutMapper.updateDashboardLayout(org.mockito.ArgumentMatchers.any())).willReturn(1);

            dashboardService.saveDashboardLayout(request);

            verify(dashboardLayoutMapper).updateDashboardLayout(org.mockito.ArgumentMatchers.any());
        }
    }
    
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
