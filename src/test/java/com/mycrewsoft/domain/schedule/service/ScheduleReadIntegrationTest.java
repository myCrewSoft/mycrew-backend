package com.mycrewsoft.domain.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.schedule.dto.response.ScheduleResponseDto;
import com.mycrewsoft.domain.schedule.mapper.IntgSchdMapper;
import com.mycrewsoft.domain.schedule.mapper.SchdTargetMapper;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.util.SecurityUtil;

@SpringBootTest
@Transactional
@DisplayName("일정 단건 조회 통합 테스트")
class ScheduleReadIntegrationTest {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private IntgSchdMapper intgSchdMapper;

    @Autowired
    private SchdTargetMapper schdTargetMapper;

    @MockBean                                    // ← 추가
    private AuthorizationService authorizationService;

    @Test
    @DisplayName("단건 조회 - 공유 대상 포함 성공")
    void readSchd_withTargets_success() {

        // given - 일정 등록
        IntgSchdVO schdVO = new IntgSchdVO();
        schdVO.setSchdClsfCd("C002");
        schdVO.setSchdNm("테스트 일정");
        schdVO.setSchdDetailCn("테스트 내용");
        schdVO.setBeginDt(LocalDateTime.of(2026, 6, 1, 10, 0));
        schdVO.setEndDt(LocalDateTime.of(2026, 6, 1, 11, 0));
        schdVO.setSchdWrtrId(1234L);
        schdVO.setAllDayYn("N");
        schdVO.setReptYn("N");
        intgSchdMapper.insertIntgSchd(schdVO);

        // given - 공유 대상 등록
        SchdTargetVO targetVO = SchdTargetVO.builder()
                .schdId(schdVO.getSchdId())
                .targetTypeCd("02")
                .targetId("1234")
                .build();
        schdTargetMapper.insertSchdTargetList(List.of(targetVO));

        // given - 권한 체크 Mock
        willDoNothing().given(authorizationService)
                .assertCurrentUserPermission(any(), any());

        // when
        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1234L);

            ScheduleResponseDto result = scheduleService.readSchd(schdVO.getSchdId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(schdVO.getSchdId());
            assertThat(result.getTitle()).isEqualTo("테스트 일정");
            assertThat(result.getTargets()).isNotEmpty();
        }
    }
}
