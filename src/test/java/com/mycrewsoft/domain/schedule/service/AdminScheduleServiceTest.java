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
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.schedule.dto.request.AdminSchdRequest;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdListResponse;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdResponse;
import com.mycrewsoft.domain.schedule.mapper.AdminSchdDtoMapper;
import com.mycrewsoft.domain.schedule.mapper.AdminSchdMapper;
import com.mycrewsoft.domain.schedule.mapper.SchdTargetMapper;
import com.mycrewsoft.domain.schedule.vo.AdminSchdListVO;
import com.mycrewsoft.domain.schedule.vo.AdminSchdSearchVO;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetDetailVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.util.SecurityUtil;

@SpringBootTest
@Transactional
@DisplayName("관리자 일정 서비스 단위 테스트")
class AdminScheduleServiceTest {

    @Autowired
    private AdminScheduleService adminScheduleService;

    @MockBean private AdminSchdMapper adminSchdMapper;
    @MockBean private SchdTargetMapper schdTargetMapper;
    @MockBean private AdminSchdDtoMapper adminSchdDtoMapper;
    @MockBean private AuthorizationService authorizationService;

    // ─────────────────────────────────────────────────────────────
    //  목록 조회
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 성공 - 검색 조건 없이 전체 조회")
    void getSchdList_success() {

        AdminSchdSearchVO search = AdminSchdSearchVO.builder().build();
        Pageable pageable = PageRequest.of(0, 10);

        AdminSchdListVO vo = createListVO(1L, "C001", "전사 워크샵");
        AdminSchdListResponse response = createListResponse(1L, "C001", "전사 워크샵");

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());
            given(adminSchdMapper.countAdminSchdList(search)).willReturn(1L);
            given(adminSchdMapper.selectAdminSchdList(search, 0L, 10)).willReturn(List.of(vo));
            given(adminSchdDtoMapper.toListResponseList(List.of(vo))).willReturn(List.of(response));

            Page<AdminSchdListResponse> result = adminScheduleService.getSchdList(search, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1L);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getSchdNm()).isEqualTo("전사 워크샵");
        }
    }

    @Test
    @DisplayName("목록 조회 성공 - 분류 코드 필터")
    void getSchdList_withClsfCd_success() {

        AdminSchdSearchVO search = AdminSchdSearchVO.builder()
                .schdClsfCdList(List.of("C005", "C006", "C007", "C008"))
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());
            given(adminSchdMapper.countAdminSchdList(search)).willReturn(0L);
            given(adminSchdMapper.selectAdminSchdList(search, 0L, 10)).willReturn(List.of());
            given(adminSchdDtoMapper.toListResponseList(List.of())).willReturn(List.of());

            Page<AdminSchdListResponse> result = adminScheduleService.getSchdList(search, pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  단건 조회
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 성공")
    void getSchd_success() {

        Long schdId = 1L;
        IntgSchdVO vo = createSchdVO(schdId, "C001", 1L);
        List<SchdTargetDetailVO> targets = List.of(new SchdTargetDetailVO());
        AdminSchdResponse response = AdminSchdResponse.builder()
                .schdId(schdId).schdNm("전사 워크샵").build();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());
            given(adminSchdMapper.selectAdminSchd(schdId)).willReturn(vo);
            given(schdTargetMapper.selectSchdTargetDetail(schdId)).willReturn(targets);
            given(adminSchdDtoMapper.toResponse(vo, targets)).willReturn(response);

            AdminSchdResponse result = adminScheduleService.getSchd(schdId);

            assertThat(result).isNotNull();
            assertThat(result.getSchdId()).isEqualTo(schdId);
            assertThat(result.getSchdNm()).isEqualTo("전사 워크샵");
        }
    }

    @Test
    @DisplayName("단건 조회 실패 - 존재하지 않는 일정")
    void getSchd_notFound_fail() {

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());
            given(adminSchdMapper.selectAdminSchd(999L)).willReturn(null);

            assertThatThrownBy(() -> adminScheduleService.getSchd(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  등록
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("일정 등록 성공")
    void createSchd_success() {

        AdminSchdRequest dto = createRequest("C001");
        IntgSchdVO vo = createSchdVO(1L, "C001", 1L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());
            given(adminSchdDtoMapper.toVO(any(), anyLong())).willReturn(vo);
            given(adminSchdMapper.insertAdminSchd(any())).willReturn(1);
            given(schdTargetMapper.insertSchdTargetList(any())).willReturn(1);

            Long result = adminScheduleService.createSchd(dto);

            assertThat(result).isEqualTo(1L);
            then(adminSchdMapper).should().insertAdminSchd(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  수정
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("일정 수정 성공")
    void modifySchd_success() {

        Long schdId = 1L;
        AdminSchdRequest dto = createRequest("C001");
        IntgSchdVO existing = createSchdVO(schdId, "C001", 1L);
        IntgSchdVO updateVO = createSchdVO(schdId, "C001", 1L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());
            given(adminSchdMapper.selectAdminSchd(schdId)).willReturn(existing);
            given(adminSchdDtoMapper.toVO(any(), anyLong())).willReturn(updateVO);
            given(adminSchdMapper.updateAdminSchd(any())).willReturn(1);
            given(schdTargetMapper.deleteSchdTarget(schdId)).willReturn(1);
            given(schdTargetMapper.insertSchdTargetList(any())).willReturn(1);

            assertThatCode(() -> adminScheduleService.modifySchd(schdId, dto))
                    .doesNotThrowAnyException();

            then(adminSchdMapper).should().updateAdminSchd(any());
            then(schdTargetMapper).should().deleteSchdTarget(schdId);
        }
    }

    @Test
    @DisplayName("일정 수정 실패 - 존재하지 않는 일정")
    void modifySchd_notFound_fail() {

        AdminSchdRequest dto = createRequest("C001");

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());
            given(adminSchdMapper.selectAdminSchd(999L)).willReturn(null);

            assertThatThrownBy(() -> adminScheduleService.modifySchd(999L, dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  삭제
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("일정 삭제 성공")
    void deleteSchd_success() {

        Long schdId = 1L;
        IntgSchdVO existing = createSchdVO(schdId, "C001", 1L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());
            given(adminSchdMapper.selectAdminSchd(schdId)).willReturn(existing);
            given(schdTargetMapper.deleteSchdTarget(schdId)).willReturn(1);
            given(adminSchdMapper.deleteAdminSchd(schdId)).willReturn(1);

            assertThatCode(() -> adminScheduleService.deleteSchd(schdId))
                    .doesNotThrowAnyException();

            then(schdTargetMapper).should().deleteSchdTarget(schdId);
            then(adminSchdMapper).should().deleteAdminSchd(schdId);
        }
    }

    @Test
    @DisplayName("일정 삭제 실패 - 존재하지 않는 일정")
    void deleteSchd_notFound_fail() {

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
            willDoNothing().given(authorizationService).assertCurrentUserPermission(any(), any());
            given(adminSchdMapper.selectAdminSchd(999L)).willReturn(null);

            assertThatThrownBy(() -> adminScheduleService.deleteSchd(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  헬퍼 메서드
    // ─────────────────────────────────────────────────────────────

    private IntgSchdVO createSchdVO(Long schdId, String clsfCd, Long wrtrId) {
        IntgSchdVO vo = new IntgSchdVO();
        vo.setSchdId(schdId);
        vo.setSchdClsfCd(clsfCd);
        vo.setSchdNm("전사 워크샵");
        vo.setBeginDt(LocalDateTime.of(2026, 6, 1, 10, 0));
        vo.setEndDt(LocalDateTime.of(2026, 6, 1, 18, 0));
        vo.setSchdWrtrId(wrtrId);
        vo.setAllDayYn("N");
        vo.setReptYn("N");
        return vo;
    }

    private AdminSchdListVO createListVO(Long schdId, String clsfCd, String schdNm) {
        AdminSchdListVO vo = new AdminSchdListVO();
        // MyBatis 자동 매핑 대상이라 reflection으로 세팅
        try {
            var f1 = AdminSchdListVO.class.getDeclaredField("schdId");
            f1.setAccessible(true); f1.set(vo, schdId);
            var f2 = AdminSchdListVO.class.getDeclaredField("schdClsfCd");
            f2.setAccessible(true); f2.set(vo, clsfCd);
            var f3 = AdminSchdListVO.class.getDeclaredField("schdNm");
            f3.setAccessible(true); f3.set(vo, schdNm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return vo;
    }

    private AdminSchdListResponse createListResponse(Long schdId, String clsfCd, String schdNm) {
        return AdminSchdListResponse.builder()
                .schdId(schdId).schdClsfCd(clsfCd).schdNm(schdNm).build();
    }

    private AdminSchdRequest createRequest(String clsfCd) {
        AdminSchdRequest req = new AdminSchdRequest();
        try {
            var f1 = AdminSchdRequest.class.getDeclaredField("schdClsfCd");
            f1.setAccessible(true); f1.set(req, clsfCd);
            var f2 = AdminSchdRequest.class.getDeclaredField("schdNm");
            f2.setAccessible(true); f2.set(req, "전사 워크샵");
            var f3 = AdminSchdRequest.class.getDeclaredField("beginDt");
            f3.setAccessible(true); f3.set(req, LocalDateTime.of(2026, 6, 1, 10, 0));
            var f4 = AdminSchdRequest.class.getDeclaredField("endDt");
            f4.setAccessible(true); f4.set(req, LocalDateTime.of(2026, 6, 1, 18, 0));
            var f5 = AdminSchdRequest.class.getDeclaredField("allDayYn");
            f5.setAccessible(true); f5.set(req, "N");
            var f6 = AdminSchdRequest.class.getDeclaredField("reptYn");
            f6.setAccessible(true); f6.set(req, "N");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return req;
    }
}