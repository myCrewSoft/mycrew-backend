package com.mycrewsoft.domain.mtng;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.verify;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mtng.dto.request.AdminMtngListRequest;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngListPageResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngStatsResponse;
import com.mycrewsoft.domain.mtng.mapper.AdminMtngDtoMapper;
import com.mycrewsoft.domain.mtng.mapper.AdminMtngMapper;
import com.mycrewsoft.domain.mtng.service.AdminMtngServiceImpl;
import com.mycrewsoft.domain.mtng.vo.AdminMtngStatsVO;
import com.mycrewsoft.domain.mtng.vo.MtngListVO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 회의 서비스 단위 테스트")
class AdminMtngServiceImplTest {

    @Mock
    private AdminMtngMapper adminMtngMapper;

    @Mock
    private AdminMtngDtoMapper adminMtngDtoMapper;

    @InjectMocks
    private AdminMtngServiceImpl adminMtngService;

    @Test
    @DisplayName("목록 조회 - 정상 조회 및 페이징 계산 성공")
    void getAdminMtngList_success() {
        // given
        AdminMtngListRequest request = new AdminMtngListRequest();

        MtngListVO vo = MtngListVO.builder()
                .mtngId(1L)
                .mtngNm("테스트 회의")
                .mtngTypeCd("MT001")
                .beginDt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .endDt(LocalDateTime.of(2026, 6, 1, 11, 0))
                .crtrId(1L)
                .crtrNm("홍길동")
                .ptcptCnt(5)
                .delYn("N")
                .build();

        given(adminMtngMapper.selectAdminMtngList(request)).willReturn(List.of(vo));
        given(adminMtngMapper.selectAdminMtngTotalCount(request)).willReturn(1);
        given(adminMtngDtoMapper.toListResponseList(List.of(vo))).willReturn(List.of());

        // when
        AdminMtngListPageResponse response = adminMtngService.getAdminMtngList(request);

        // then
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getCurrentPage()).isEqualTo(request.getPage());
        assertThat(response.getTotalPages()).isEqualTo(1);
        verify(adminMtngMapper).selectAdminMtngList(request);
        verify(adminMtngMapper).selectAdminMtngTotalCount(request);
    }

    @Test
    @DisplayName("상세 조회 - 존재하지 않는 회의 ID면 예외 발생")
    void getAdminMtngDetail_notFound_throwsException() {
        // given
        Long mtngId = 999L;
        given(adminMtngMapper.selectAdminMtngDetail(mtngId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> adminMtngService.getAdminMtngDetail(mtngId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MTNG_NOT_FOUND);
    }

    @Test
    @DisplayName("상세 조회 - 정상 조회 및 참여자 목록 포함 성공")
    void getAdminMtngDetail_success() {
        // given
        Long mtngId = 1L;

        MtngListVO vo = MtngListVO.builder()
                .mtngId(mtngId)
                .mtngNm("테스트 회의")
                .mtngTypeCd("MT001")
                .beginDt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .endDt(LocalDateTime.of(2026, 6, 1, 11, 0))
                .crtrId(1L)
                .crtrNm("홍길동")
                .delYn("N")
                .build();

        AdminMtngDetailResponse baseResponse = AdminMtngDetailResponse.builder()
                .mtngId(mtngId)
                .mtngNm("테스트 회의")
                .ptcpts(null)
                .build();

        given(adminMtngMapper.selectAdminMtngDetail(mtngId)).willReturn(vo);
        given(adminMtngMapper.selectAdminMtngPtcptList(mtngId)).willReturn(List.of());
        given(adminMtngDtoMapper.toDetailResponse(vo)).willReturn(baseResponse);
        given(adminMtngDtoMapper.toPtcptResponseList(List.of())).willReturn(List.of());

        // when
        AdminMtngDetailResponse response = adminMtngService.getAdminMtngDetail(mtngId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMtngId()).isEqualTo(mtngId);
        assertThat(response.getPtcpts()).isNotNull();
    }

    @Test
    @DisplayName("강제 종료 - 진행중이 아닌 회의면 예외 발생")
    void forceEndMtng_notInProgress_throwsException() {
        // given
        Long mtngId = 1L;
        willReturn(0).given(adminMtngMapper).updateMtngForceEnd(mtngId);

        // when & then
        assertThatThrownBy(() -> adminMtngService.forceEndMtng(mtngId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MTNG_NOT_IN_PROGRESS);
    }

    @Test
    @DisplayName("강제 종료 - 진행중 회의 정상 종료 성공")
    void forceEndMtng_success() {
        // given
        Long mtngId = 1L;
        willReturn(1).given(adminMtngMapper).updateMtngForceEnd(mtngId);

        // when
        adminMtngService.forceEndMtng(mtngId);

        // then
        verify(adminMtngMapper).updateMtngForceEnd(mtngId);
    }

    @Test
    @DisplayName("상단 카드 통계 조회 성공")
    void getAdminMtngStats_success() {
        // given
        AdminMtngStatsVO statsVO = AdminMtngStatsVO.builder()
                .todayScheduledCnt(5)
                .inProgressCnt(3)
                .completedCnt(8)
                .aiPendingCnt(2)
                .build();

        AdminMtngStatsResponse statsResponse = AdminMtngStatsResponse.builder()
                .todayScheduledCnt(5)
                .inProgressCnt(3)
                .completedCnt(8)
                .aiPendingCnt(2)
                .build();

        given(adminMtngMapper.selectAdminMtngStats()).willReturn(statsVO);
        given(adminMtngDtoMapper.toStatsResponse(statsVO)).willReturn(statsResponse);

        // when
        AdminMtngStatsResponse response = adminMtngService.getAdminMtngStats();

        // then
        assertThat(response.getTodayScheduledCnt()).isEqualTo(5);
        assertThat(response.getInProgressCnt()).isEqualTo(3);
        assertThat(response.getCompletedCnt()).isEqualTo(8);
        assertThat(response.getAiPendingCnt()).isEqualTo(2);
    }
}