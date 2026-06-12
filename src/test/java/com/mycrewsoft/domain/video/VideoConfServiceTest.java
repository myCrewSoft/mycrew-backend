//package com.mycrewsoft.domain.video;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.willDoNothing;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import com.mycrewsoft.common.exception.CustomException;
//import com.mycrewsoft.common.exception.ErrorCode;
//import com.mycrewsoft.domain.video.config.LiveKitTokenProvider;
//import com.mycrewsoft.domain.video.dto.request.VideoConfCreateRequest;
//import com.mycrewsoft.domain.video.dto.request.VideoMomAprvlRequest;
//import com.mycrewsoft.domain.video.dto.request.VideoMomUpdateRequest;
//import com.mycrewsoft.domain.video.dto.response.VideoConfResponse;
//import com.mycrewsoft.domain.video.dto.response.VideoMomResponse;
//import com.mycrewsoft.domain.video.mapper.VideoConfDtoMapper;
//import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
//import com.mycrewsoft.domain.video.service.VideoConfServiceImpl;
//import com.mycrewsoft.domain.video.vo.VideoConfListVO;
//import com.mycrewsoft.domain.video.vo.VideoConfVO;
//import com.mycrewsoft.domain.video.vo.VideoMomAprvlVO;
//import com.mycrewsoft.domain.video.vo.VideoMomVO;
//import com.mycrewsoft.domain.video.vo.VideoPtcptVO;
//import com.mycrewsoft.domain.video.vo.VideoRcrdgVO;
//import com.mycrewsoft.security.util.SecurityUtil;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("화상회의 서비스 단위 테스트")
//class VideoConfServiceTest {
//
//    @InjectMocks
//    private VideoConfServiceImpl videoConfService;
//
//    @Mock private VideoConfMapper videoConfMapper;
//    @Mock private VideoConfDtoMapper videoConfDtoMapper;
//    @Mock private LiveKitTokenProvider liveKitTokenProvider;
//
//    // ─────────────────────────────────────────────────────────────
//    // 공통 픽스처 생성 메서드
//    // ─────────────────────────────────────────────────────────────
//
//    private VideoConfListVO createConfVO(Long vconfId, Long crtrId, String sttusCd) {
//    	VideoConfListVO vo = new VideoConfListVO();
//        vo.setVconfId(vconfId);
//        vo.setCrtrId(crtrId);
//        vo.setVconfNm("테스트 회의");
//        vo.setRoomNm("meeting-" + vconfId);
//        vo.setConfSttusCd(sttusCd);
//        vo.setBeginDt(LocalDateTime.now());
//        vo.setEndDt(LocalDateTime.now().plusHours(1));
//        vo.setVideoPtcpt(List.of(createPtcptVO(1L, vconfId, crtrId)));
//        return vo;
//    }
//
//    private VideoPtcptVO createPtcptVO(Long vconfPtcptId, Long vconfId, Long empId) {
//        VideoPtcptVO vo = new VideoPtcptVO();
//        vo.setVconfPtcptId(vconfPtcptId);
//        vo.setVconfId(vconfId);
//        vo.setEmpId(empId);
//        return vo;
//    }
//
//    private VideoMomVO createMomVO(Long momId, Long vconfId, String sttusCd) {
//        VideoMomVO vo = new VideoMomVO();
//        vo.setMomId(momId);
//        vo.setVconfId(vconfId);
//        vo.setMomCn("테스트 회의록 내용");
//        vo.setMomSttusCd(sttusCd);
//        vo.setEdtrId(1L);
//        vo.setAprvlRoundNo(0);
//        vo.setVideoMomAprvl(List.of());
//        vo.setVideoMomHist(List.of());
//        return vo;
//    }
//
//    private VideoMomAprvlVO createAprvlVO(Long aprvlId, Long ptcptId, String sttusCd, Integer roundNo) {
//        VideoMomAprvlVO vo = new VideoMomAprvlVO();
//        vo.setAprvlId(aprvlId);
//        vo.setPtcptId(ptcptId);
//        vo.setAprvlSttusCd(sttusCd);
//        vo.setAprvlRoundNo(roundNo);
//        return vo;
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // CREATE
//    // ─────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("화상회의 생성 성공")
//    void createConf_success() {
//        // given
//        VideoConfCreateRequest request = VideoConfCreateRequest.builder()
//                .vconfNm("테스트 회의")
//                .beginDt(LocalDateTime.now())
//                .endDt(LocalDateTime.now().plusHours(1))
//                .ptcptEmpIds(List.of(2L, 3L))
//                .build();
//
//        VideoConfListVO confVO = createConfVO(1L, 1L, "01");
//        VideoConfResponse response = new VideoConfResponse();
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            given(videoConfDtoMapper.toConfVO(request)).willReturn(confVO);
//            willDoNothing().given(videoConfMapper).insertConf(any());
//            willDoNothing().given(videoConfMapper).updateRoomNm(anyLong(), any());
//            willDoNothing().given(videoConfMapper).insertPtcpt(any());
//            given(videoConfMapper.selectConfById(anyLong())).willReturn(confVO);
//            given(videoConfDtoMapper.toConfResponse(confVO)).willReturn(response);
//
//            // when
//            VideoConfResponse result = videoConfService.createConf(request);
//
//            // then
//            assertThat(result).isNotNull();
//            verify(videoConfMapper, times(1)).insertConf(any());
//            verify(videoConfMapper, times(1)).updateRoomNm(anyLong(), any());
//        }
//    }
//
//    @Test
//    @DisplayName("화상회의 생성 - 생성자가 참여자 목록에 없으면 자동 추가")
//    void createConf_addsCreatorToPtcpt() {
//        // given - ptcptEmpIds에 생성자(1L) 없음
//        VideoConfCreateRequest request = VideoConfCreateRequest.builder()
//                .vconfNm("테스트 회의")
//                .beginDt(LocalDateTime.now())
//                .endDt(LocalDateTime.now().plusHours(1))
//                .ptcptEmpIds(List.of(2L, 3L))
//                .build();
//
//        VideoConfVO confVO = createConfVO(1L, 1L, "01");
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            given(videoConfDtoMapper.toConfVO(request)).willReturn(confVO);
//            willDoNothing().given(videoConfMapper).insertConf(any());
//            willDoNothing().given(videoConfMapper).updateRoomNm(anyLong(), any());
//            willDoNothing().given(videoConfMapper).insertPtcpt(any());
//            given(videoConfMapper.selectConfById(anyLong())).willReturn(confVO);
//            given(videoConfDtoMapper.toConfResponse(confVO)).willReturn(new VideoConfResponse());
//
//            // when
//            videoConfService.createConf(request);
//
//            // then - 생성자 포함 총 3명 insertPtcpt 호출
//            verify(videoConfMapper, times(3)).insertPtcpt(any());
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // READ
//    // ─────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("화상회의 단건 조회 성공")
//    void getConf_success() {
//        // given
//        VideoConfVO confVO = createConfVO(1L, 1L, "01");
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            given(videoConfMapper.selectConfById(1L)).willReturn(confVO);
//            given(videoConfDtoMapper.toConfResponse(confVO)).willReturn(new VideoConfResponse());
//
//            // when
//            VideoConfResponse result = videoConfService.getConf(1L);
//
//            // then
//            assertThat(result).isNotNull();
//        }
//    }
//
//    @Test
//    @DisplayName("화상회의 단건 조회 실패 - 존재하지 않는 회의")
//    void getConf_notFound() {
//        // given
//        given(videoConfMapper.selectConfById(anyLong())).willReturn(null);
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            // when & then
//            assertThatThrownBy(() -> videoConfService.getConf(99L))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VIDEO_CONF_NOT_FOUND);
//        }
//    }
//
//    @Test
//    @DisplayName("화상회의 단건 조회 실패 - 참여자 아닌 경우")
//    void getConf_accessDenied() {
//        // given - 회의 참여자는 empId=1L인데 현재 로그인 사용자는 99L
//        VideoConfVO confVO = createConfVO(1L, 1L, "01");
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(99L);
//
//            given(videoConfMapper.selectConfById(1L)).willReturn(confVO);
//
//            // when & then
//            assertThatThrownBy(() -> videoConfService.getConf(1L))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VIDEO_ACCESS_DENIED);
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // END CONF
//    // ─────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("화상회의 종료 성공 - 호스트가 종료")
//    void endConf_success() {
//        // given
//        VideoConfVO confVO = createConfVO(1L, 1L, "02");
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            given(videoConfMapper.selectConfById(1L)).willReturn(confVO);
//            willDoNothing().given(videoConfMapper).updateConfSttus(anyLong(), any());
//
//            // when
//            videoConfService.endConf(1L);
//
//            // then
//            verify(videoConfMapper, times(1)).updateConfSttus(1L, "03");
//        }
//    }
//
//    @Test
//    @DisplayName("화상회의 종료 실패 - 호스트 아닌 경우")
//    void endConf_accessDenied() {
//        // given - crtrId=1L인데 현재 로그인 사용자는 99L
//        VideoConfVO confVO = createConfVO(1L, 1L, "02");
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(99L);
//
//            given(videoConfMapper.selectConfById(1L)).willReturn(confVO);
//
//            // when & then
//            assertThatThrownBy(() -> videoConfService.endConf(1L))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VIDEO_ACCESS_DENIED);
//
//            verify(videoConfMapper, never()).updateConfSttus(anyLong(), any());
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // MOM UPDATE
//    // ─────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("회의록 수정 성공")
//    void updateMom_success() {
//        // given
//        VideoMomUpdateRequest request = VideoMomUpdateRequest.builder()
//                .momCn("수정된 회의록 내용")
//                .build();
//
//        VideoMomVO momVO    = createMomVO(1L, 1L, "02");
//        VideoMomVO updateVO = createMomVO(1L, 1L, "02");
//        VideoMomResponse response = new VideoMomResponse();
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            given(videoConfMapper.selectMomByVconfId(1L)).willReturn(momVO).willReturn(updateVO);
//            given(videoConfDtoMapper.toMomVO(request)).willReturn(updateVO);
//            willDoNothing().given(videoConfMapper).insertMomHist(any());
//            willDoNothing().given(videoConfMapper).updateMom(any());
//            given(videoConfDtoMapper.toMomResponse(updateVO)).willReturn(response);
//
//            // when
//            VideoMomResponse result = videoConfService.updateMom(1L, request);
//
//            // then
//            assertThat(result).isNotNull();
//            verify(videoConfMapper, times(1)).insertMomHist(any());
//            verify(videoConfMapper, times(1)).updateMom(any());
//        }
//    }
//
//    @Test
//    @DisplayName("회의록 수정 실패 - 확정된 회의록")
//    void updateMom_alreadyConfirmed() {
//        // given - 상태코드 04(확정)
//        VideoMomVO momVO = createMomVO(1L, 1L, "04");
//        VideoMomUpdateRequest request = VideoMomUpdateRequest.builder()
//                .momCn("수정 시도")
//                .build();
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            given(videoConfMapper.selectMomByVconfId(1L)).willReturn(momVO);
//
//            // when & then
//            assertThatThrownBy(() -> videoConfService.updateMom(1L, request))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VIDEO_MOM_ALREADY_CONFIRMED);
//
//            verify(videoConfMapper, never()).updateMom(any());
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // MOM REVIEW
//    // ─────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("회의록 검토 요청 성공")
//    void requestMomReview_success() {
//        // given
//        VideoConfVO confVO = createConfVO(1L, 1L, "02");
//        VideoMomVO momVO   = createMomVO(1L, 1L, "02");
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            given(videoConfMapper.selectMomByVconfId(1L)).willReturn(momVO);
//            given(videoConfMapper.selectConfById(1L)).willReturn(confVO);
//            willDoNothing().given(videoConfMapper).insertAprvlList(any());
//            willDoNothing().given(videoConfMapper).updateMom(any());
//
//            // when
//            videoConfService.requestMomReview(1L);
//
//            // then
//            verify(videoConfMapper, times(1)).insertAprvlList(any());
//            verify(videoConfMapper, times(1)).updateMom(any());
//        }
//    }
//
//    @Test
//    @DisplayName("회의록 검토 요청 실패 - 담당자 아닌 경우")
//    void requestMomReview_notEditor() {
//        // given - edtrId=1L인데 현재 로그인 사용자는 99L
//        VideoMomVO momVO = createMomVO(1L, 1L, "02");
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(99L);
//
//            given(videoConfMapper.selectMomByVconfId(1L)).willReturn(momVO);
//
//            // when & then
//            assertThatThrownBy(() -> videoConfService.requestMomReview(1L))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
//
//            verify(videoConfMapper, never()).insertAprvlList(any());
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // MOM APPROVE
//    // ─────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("회의록 결재 - 전원 승인 시 자동 확정")
//    void approveMom_allApproved_confirmed() {
//        // given - 참여자 1명, 본인이 승인하면 전원 승인
//        VideoMomVO momVO = createMomVO(1L, 1L, "03");
//        momVO.setAprvlRoundNo(1);
//
//        VideoMomAprvlVO aprvlVO = createAprvlVO(1L, 1L, "01", 1);
//        VideoMomAprvlRequest request = VideoMomAprvlRequest.builder()
//                .aprvlSttusCd("02")
//                .build();
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            given(videoConfMapper.selectMomByVconfId(1L)).willReturn(momVO);
//            given(videoConfMapper.selectAprvlListByMomId(1L)).willReturn(List.of(aprvlVO));
//            willDoNothing().given(videoConfMapper).updateAprvl(any());
//            willDoNothing().given(videoConfMapper).updateMom(any());
//
//            // when
//            videoConfService.approveMom(1L, request);
//
//            // then - 전원 승인이므로 회의록 확정 updateMom 호출
//            verify(videoConfMapper, times(1)).updateAprvl(any());
//            verify(videoConfMapper, times(1)).updateMom(any());
//        }
//    }
//
//    @Test
//    @DisplayName("회의록 결재 실패 - 이미 결재 처리된 항목")
//    void approveMom_alreadyDone() {
//        // given - 이미 승인(02) 상태인 결재 행
//        VideoMomVO momVO = createMomVO(1L, 1L, "03");
//        momVO.setAprvlRoundNo(1);
//
//        VideoMomAprvlVO aprvlVO = createAprvlVO(1L, 1L, "02", 1); // 이미 승인
//        VideoMomAprvlRequest request = VideoMomAprvlRequest.builder()
//                .aprvlSttusCd("02")
//                .build();
//
//        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
//            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(1L);
//
//            given(videoConfMapper.selectMomByVconfId(1L)).willReturn(momVO);
//            given(videoConfMapper.selectAprvlListByMomId(1L)).willReturn(List.of(aprvlVO));
//
//            // when & then
//            assertThatThrownBy(() -> videoConfService.approveMom(1L, request))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VIDEO_APRVL_ALREADY_DONE);
//
//            verify(videoConfMapper, never()).updateAprvl(any());
//        }
//    }
//}