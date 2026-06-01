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
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.schedule.dto.request.ScheduleRequestDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleResponseDto;
import com.mycrewsoft.domain.schedule.mapper.IntgSchdMapper;
import com.mycrewsoft.domain.schedule.mapper.SchdTargetMapper;
import com.mycrewsoft.domain.schedule.mapper.ScheduleMapper;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.util.SecurityUtil;

@SpringBootTest
@Transactional
@DisplayName("일정 서비스 단위 테스트")
class ScheduleServiceTest {

    @Autowired
    private ScheduleService scheduleService;

    @MockBean private IntgSchdMapper intgSchdMapper;
    @MockBean private SchdTargetMapper schdTargetMapper;
    @MockBean private ScheduleMapper scheduleMapper;
    @MockBean private EmployeeMapper employeeMapper;
    @MockBean private AuthorizationService authorizationService;

    // ─────────────────────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("개인 일정 등록 성공")
    void createSchd_personal_success() {

        // given
        ScheduleRequestDto dto = createRequestDto("C002", null, null);
        IntgSchdVO schdVO = createSchdVO(1L, "C002", 1234L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);
            secUtil.when(SecurityUtil::isCurrentExec).thenReturn(false);

            given(scheduleMapper.toVo(any(), anyLong())).willReturn(schdVO);
            willDoNothing().given(intgSchdMapper).insertIntgSchd(any());
            given(schdTargetMapper.insertSchdTargetList(any())).willReturn(1);

            // when
            Long result = scheduleService.createSchd(dto);

            // then
            assertThat(result).isEqualTo(1L);
            then(intgSchdMapper).should().insertIntgSchd(any());
            then(schdTargetMapper).should().insertSchdTargetList(any());
        }
    }

    @Test
    @DisplayName("전사 공통 일정 등록 - 간부가 아니면 실패")
    void createSchd_all_notExec_fail() {

        // given
        ScheduleRequestDto dto = createRequestDto("C001", null, null);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);
            secUtil.when(SecurityUtil::isCurrentExec).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> scheduleService.createSchd(dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
        }
    }

    @Test
    @DisplayName("전사 공통 일정 등록 - 간부면 성공")
    void createSchd_all_exec_success() {

        // given
        ScheduleRequestDto dto = createRequestDto("C001", null, null);
        IntgSchdVO schdVO = createSchdVO(1L, "C001", 1234L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);
            secUtil.when(SecurityUtil::isCurrentExec).thenReturn(true);

            given(scheduleMapper.toVo(any(), anyLong())).willReturn(schdVO);
            willDoNothing().given(intgSchdMapper).insertIntgSchd(any());
            given(schdTargetMapper.insertSchdTargetList(any())).willReturn(1);

            // when
            Long result = scheduleService.createSchd(dto);

            // then
            assertThat(result).isEqualTo(1L);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("일정 단건 조회 성공")
    void readSchd_success() {

        // given
        Long schdId = 1L;
        IntgSchdVO schdVO = createSchdVO(schdId, "C002", 1234L);
        ScheduleResponseDto responseDto = ScheduleResponseDto.builder()
        		.id(1L)
                .title("테스트 일정")
                .scheduleTypeCode("C002")
                .start(LocalDateTime.of(2026, 6, 1, 10, 0))
                .end(LocalDateTime.of(2026, 6, 1, 11, 0))
                .allDay(false)
                .repeat(false)
                .build();
        
        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);

            willDoNothing().given(authorizationService)
                    .assertCurrentUserPermission(any(), any());
            given(intgSchdMapper.selectIntgSchd(schdId)).willReturn(schdVO);
            given(scheduleMapper.toResponseDto(schdVO)).willReturn(responseDto);

            // when
            ScheduleResponseDto result = scheduleService.readSchd(schdId);

            // then
            assertThat(result).isNotNull();
        }
    }

    @Test
    @DisplayName("일정 단건 조회 - 본인 일정 아니면 실패")
    void readSchd_notOwner_fail() {

        // given
        Long schdId = 1L;
        IntgSchdVO schdVO = createSchdVO(schdId, "C002", 9999L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);

            willDoNothing().given(authorizationService)
                    .assertCurrentUserPermission(any(), any());
            given(intgSchdMapper.selectIntgSchd(schdId)).willReturn(schdVO);

            // when & then
            assertThatThrownBy(() -> scheduleService.readSchd(schdId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.NOT_SCHEDULE_OWNER.getMessage());
        }
    }

    @Test
    @DisplayName("일정 목록 조회 성공")
    void readSchdList_success() {

        // given
        LocalDateTime beginDt = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime endDt   = LocalDateTime.of(2026, 6, 30, 23, 59);
        List<IntgSchdVO> schdList = List.of(createSchdVO(1L, "C002", 1234L));
        List<ScheduleResponseDto> responseDtos = List.of(
                ScheduleResponseDto.builder()
                        .id(1L)
                        .title("테스트 일정")
                        .scheduleTypeCode("C002")
                        .start(LocalDateTime.of(2026, 6, 1, 10, 0))
                        .end(LocalDateTime.of(2026, 6, 1, 11, 0))
                        .allDay(false)
                        .repeat(false)
                        .build()
        );
        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);
            secUtil.when(SecurityUtil::isCurrentExec).thenReturn(false);

            willDoNothing().given(authorizationService)
                    .assertCurrentUserPermission(any(), any());
            given(employeeMapper.selectEmpDeptCodeByEmpId(anyLong())).willReturn("D0001");
            given(intgSchdMapper.selectIntgSchdList(any())).willReturn(schdList);
            given(scheduleMapper.toDtoList(schdList)).willReturn(responseDtos);

            // when
            List<ScheduleResponseDto> result = scheduleService.readSchdList(beginDt, endDt);

            // then
            assertThat(result).hasSize(1);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("일정 수정 성공")
    void modifySchd_success() {

        // given
        Long schdId = 1L;
        ScheduleRequestDto dto = createRequestDto("C002", null, null);
        IntgSchdVO schdVO   = createSchdVO(schdId, "C002", 1234L);
        IntgSchdVO updateVO = createSchdVO(schdId, "C002", 1234L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);

            given(intgSchdMapper.selectIntgSchd(schdId)).willReturn(schdVO);
            given(scheduleMapper.toVo(any(), anyLong())).willReturn(updateVO);
            given(intgSchdMapper.updateIntgSchd(any())).willReturn(1);
            given(schdTargetMapper.deleteSchdTarget(schdId)).willReturn(1);
            given(schdTargetMapper.insertSchdTargetList(any())).willReturn(1);

            // when & then
            assertThatCode(() -> scheduleService.modifySchd(schdId, dto))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("일정 수정 - 본인 일정 아니면 실패")
    void modifySchd_notOwner_fail() {

        // given
        Long schdId = 1L;
        ScheduleRequestDto dto = createRequestDto("C002", null, null);
        IntgSchdVO schdVO = createSchdVO(schdId, "C002", 9999L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);

            given(intgSchdMapper.selectIntgSchd(schdId)).willReturn(schdVO);

            // when & then
            assertThatThrownBy(() -> scheduleService.modifySchd(schdId, dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.NOT_SCHEDULE_OWNER.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("일정 삭제 성공")
    void deleteSchd_success() {

        // given
        Long schdId = 1L;
        IntgSchdVO schdVO = createSchdVO(schdId, "C002", 1234L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);

            given(intgSchdMapper.selectIntgSchd(schdId)).willReturn(schdVO);
            given(schdTargetMapper.deleteSchdTarget(schdId)).willReturn(1);
            given(intgSchdMapper.deleteIntgSchd(schdId)).willReturn(1);

            // when & then
            assertThatCode(() -> scheduleService.deleteSchd(schdId))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("일정 삭제 - 존재하지 않는 일정이면 실패")
    void deleteSchd_notFound_fail() {

        // given
        Long schdId = 999L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);

            given(intgSchdMapper.selectIntgSchd(schdId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> scheduleService.deleteSchd(schdId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
        }
    }

    @Test
    @DisplayName("일정 삭제 - 본인 일정 아니면 실패")
    void deleteSchd_notOwner_fail() {

        // given
        Long schdId = 1L;
        IntgSchdVO schdVO = createSchdVO(schdId, "C002", 9999L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);

            given(intgSchdMapper.selectIntgSchd(schdId)).willReturn(schdVO);

            // when & then
            assertThatThrownBy(() -> scheduleService.deleteSchd(schdId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.NOT_SCHEDULE_OWNER.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  테스트 픽스처
    // ─────────────────────────────────────────────────────────────

    private ScheduleRequestDto createRequestDto(String schdClsfCd, String deptCd, Long projId) {
        ScheduleRequestDto dto = new ScheduleRequestDto();
        try {
            setField(dto, "schdClsfCd", schdClsfCd);
            setField(dto, "schdNm",     "테스트 일정");
            setField(dto, "beginDt",    LocalDateTime.of(2026, 6, 1, 10, 0));
            setField(dto, "endDt",      LocalDateTime.of(2026, 6, 1, 11, 0));
            setField(dto, "allDayYn",   "N");
            setField(dto, "reptYn",     "N");
            setField(dto, "deptCd",     deptCd);
            setField(dto, "projId",     projId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dto;
    }

    private IntgSchdVO createSchdVO(Long schdId, String schdClsfCd, Long schdWrtrId) {
        IntgSchdVO vo = new IntgSchdVO();
        try {
            setField(vo, "schdId",     schdId);
            setField(vo, "schdClsfCd", schdClsfCd);
            setField(vo, "schdWrtrId", schdWrtrId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return vo;
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}