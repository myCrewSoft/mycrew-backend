package com.mycrewsoft.domain.schedule.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.schedule.dto.response.AdminSchdListResponse;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdResponse;
import com.mycrewsoft.domain.schedule.mapper.AdminSchdMapper;
import com.mycrewsoft.domain.schedule.mapper.SchdTargetMapper;
import com.mycrewsoft.domain.schedule.vo.AdminSchdSearchVO;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.util.SecurityUtil;

@SpringBootTest
@Transactional
@DisplayName("관리자 일정 통합 테스트")
class AdminScheduleIntegrationTest {

    @Autowired
    private AdminScheduleService adminScheduleService;

    @Autowired
    private AdminSchdMapper adminSchdMapper;

    @Autowired
    private SchdTargetMapper schdTargetMapper;

    @MockBean
    private AuthorizationService authorizationService;

    @Test
    @DisplayName("일정 등록 후 단건 조회 - 공유 대상 포함 성공")
    void createAndGetSchd_withTargets_success() {

        willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());

        IntgSchdVO vo = new IntgSchdVO();
        vo.setSchdClsfCd("C001");
        vo.setSchdNm("전사 워크샵");
        vo.setSchdDetailCn("2박 3일 전사 워크샵");
        vo.setBeginDt(LocalDateTime.of(2026, 6, 1, 10, 0));
        vo.setEndDt(LocalDateTime.of(2026, 6, 3, 18, 0));
        vo.setSchdWrtrId(1L);
        vo.setAllDayYn("N");
        vo.setReptYn("N");
        adminSchdMapper.insertAdminSchd(vo);

        SchdTargetVO target = SchdTargetVO.builder()
                .schdId(vo.getSchdId())
                .targetTypeCd("01")
                .targetId("0")
                .build();
        schdTargetMapper.insertSchdTargetList(List.of(target));

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

            AdminSchdResponse result = adminScheduleService.getSchd(vo.getSchdId());

            assertThat(result).isNotNull();
            assertThat(result.getSchdId()).isEqualTo(vo.getSchdId());
            assertThat(result.getSchdNm()).isEqualTo("전사 워크샵");
            assertThat(result.getSchdClsfCd()).isEqualTo("C001");
            assertThat(result.getTargets()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("목록 조회 - 분류 코드 필터 적용 성공")
    void getSchdList_clsfCdFilter_success() {

        willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());

        IntgSchdVO orgVo = new IntgSchdVO();
        orgVo.setSchdClsfCd("C001");
        orgVo.setSchdNm("전사 일정");
        orgVo.setBeginDt(LocalDateTime.now());
        orgVo.setEndDt(LocalDateTime.now().plusHours(2));
        orgVo.setSchdWrtrId(1L);
        orgVo.setAllDayYn("N");
        orgVo.setReptYn("N");
        adminSchdMapper.insertAdminSchd(orgVo);

        IntgSchdVO autoVo = new IntgSchdVO();
        autoVo.setSchdClsfCd("C005");
        autoVo.setSchdNm("프로젝트 일정");
        autoVo.setBeginDt(LocalDateTime.now());
        autoVo.setEndDt(LocalDateTime.now().plusHours(2));
        autoVo.setSchdWrtrId(1L);
        autoVo.setAllDayYn("N");
        autoVo.setReptYn("N");
        adminSchdMapper.insertAdminSchd(autoVo);

        AdminSchdSearchVO search = AdminSchdSearchVO.builder()
                .schdClsfCdList(List.of("C001"))
                .build();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

            Page<AdminSchdListResponse> result = adminScheduleService.getSchdList(
                    search, PageRequest.of(0, 10));

            assertThat(result.getContent())
                    .allMatch(r -> r.getSchdClsfCd().equals("C001"));
        }
    }

    @Test
    @DisplayName("일정 삭제 후 단건 조회 - 논리 삭제 확인")
    void deleteSchd_thenGetSchd_notFound() {

        willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());

        IntgSchdVO vo = new IntgSchdVO();
        vo.setSchdClsfCd("C001");
        vo.setSchdNm("삭제 테스트 일정");
        vo.setBeginDt(LocalDateTime.now());
        vo.setEndDt(LocalDateTime.now().plusHours(1));
        vo.setSchdWrtrId(1L);
        vo.setAllDayYn("N");
        vo.setReptYn("N");
        adminSchdMapper.insertAdminSchd(vo);
        Long schdId = vo.getSchdId();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);

            adminScheduleService.deleteSchd(schdId);

            assertThatThrownBy(() -> adminScheduleService.getSchd(schdId))
                    .isInstanceOf(com.mycrewsoft.common.exception.CustomException.class);
        }
    }
}