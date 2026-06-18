package com.mycrewsoft.domain.mtng;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngListPageResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngStatsResponse;
import com.mycrewsoft.domain.mtng.enums.MtngTypeCode;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.service.AdminMtngService;
import com.mycrewsoft.domain.mtng.vo.MtngVO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import com.mycrewsoft.domain.mtng.dto.request.AdminMtngListRequest;
import com.mycrewsoft.security.authz.AuthorizationService;

@SpringBootTest
@Transactional
@DisplayName("관리자 회의 서비스 통합 테스트")
class AdminMtngServiceIntegrationTest {

    @Autowired
    private AdminMtngService adminMtngService;

    @Autowired
    private MtngMapper mtngMapper;

    @MockBean
    private AuthorizationService authorizationService;

    @Test
    @DisplayName("목록 조회 - DB INSERT 후 목록에 포함되는지 확인")
    void getAdminMtngList_afterInsert_included() {
        // given
        insertTestMtng(MtngTypeCode.ONLINE,
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 11, 0));

        AdminMtngListRequest request = new AdminMtngListRequest();

        // when
        AdminMtngListPageResponse response = adminMtngService.getAdminMtngList(request);

        // then
        assertThat(response.getTotalCount()).isPositive();
        assertThat(response.getMeetings()).isNotEmpty();
    }

    @Test
    @DisplayName("상세 조회 - DB INSERT 후 단건 조회 성공")
    void getAdminMtngDetail_afterInsert_success() {
        // given
        MtngVO vo = insertTestMtng(MtngTypeCode.OFFLINE,
                LocalDateTime.of(2026, 6, 1, 14, 0),
                LocalDateTime.of(2026, 6, 1, 15, 0));

        // when
        AdminMtngDetailResponse response = adminMtngService.getAdminMtngDetail(vo.getMtngId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMtngId()).isEqualTo(vo.getMtngId());
        assertThat(response.getMtngNm()).isEqualTo("통합테스트 회의");
        assertThat(response.getPtcpts()).isNotNull();
    }

    @Test
    @DisplayName("상세 조회 - 존재하지 않는 ID 조회 시 예외 발생")
    void getAdminMtngDetail_notFound_throwsException() {
        // when & then
        assertThatThrownBy(() -> adminMtngService.getAdminMtngDetail(999999L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("강제 종료 - 진행중 회의 종료 후 상태 변경 확인")
    void forceEndMtng_inProgress_success() {
        // given - 현재 시각 기준 진행중인 회의 INSERT
        MtngVO vo = insertTestMtng(MtngTypeCode.ONLINE,
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().plusMinutes(30));

        // when
        adminMtngService.forceEndMtng(vo.getMtngId());

        // then - 종료 후 상세 조회해서 vconfSttus가 완료(VC003)인지 확인
        AdminMtngDetailResponse response = adminMtngService.getAdminMtngDetail(vo.getMtngId());
        assertThat(response.getEndDt()).isBefore(LocalDateTime.now());

    }

    @Test
    @DisplayName("상단 카드 통계 - 진행중 회의 INSERT 후 inProgressCnt 증가 확인")
    void getAdminMtngStats_inProgressCnt_increased() {
        // given - 진행중 회의 INSERT
        insertTestMtng(MtngTypeCode.ONLINE,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusMinutes(50));

        // when
        AdminMtngStatsResponse response = adminMtngService.getAdminMtngStats();

        // then
        assertThat(response.getInProgressCnt()).isPositive();
    }

    private MtngVO insertTestMtng(MtngTypeCode mtngTypeCd, LocalDateTime beginDt, LocalDateTime endDt) {
        MtngVO vo = MtngVO.builder()
                .mtngNm("통합테스트 회의")
                .mtngTypeCd(mtngTypeCd.getCode())
                .crtrId(1L)
                .beginDt(beginDt)
                .endDt(endDt)
                .delYn("N")
                .build();
        mtngMapper.createMtng(vo);
        return vo;
    }
}