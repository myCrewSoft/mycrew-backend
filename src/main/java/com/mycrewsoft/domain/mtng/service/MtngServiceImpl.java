package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mtng.dto.request.MtngCreateRequest;
import com.mycrewsoft.domain.mtng.dto.request.MtngListRequest;
import com.mycrewsoft.domain.mtng.dto.request.MtngUpdateRequest;
import com.mycrewsoft.domain.mtng.dto.response.MtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.MtngListResponse;
import com.mycrewsoft.domain.mtng.dto.response.MtngPtcptResponse;
import com.mycrewsoft.domain.mtng.enums.MtngSttus;
import com.mycrewsoft.domain.mtng.enums.MtngTypeCode;
import com.mycrewsoft.domain.mtng.mapper.MtngDtoMapper;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngListVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptVO;
import com.mycrewsoft.domain.mtng.vo.MtngVO;
import com.mycrewsoft.domain.reservation.dto.request.ReservationCreateRequest;
import com.mycrewsoft.domain.reservation.service.ReservationService;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoConfVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MtngServiceImpl implements MtngService {

    private final MtngCreatorFactory mtngCreatorFactory;
    private final MtngMapper mtngMapper;
    private final MtngDtoMapper mtngDtoMapper;
    private final ReservationService reservationService;
    private final VideoConfMapper videoConfMapper;


    @Override
    @Transactional
    public Long createMtng(MtngCreateRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();

        MtngTypeCode typeCode = MtngTypeCode.fromCode(request.getMtngTypeCd());

        AbstractMtngCreator creator = mtngCreatorFactory.getCreator(typeCode);

        MtngCreateContext context = MtngCreateContext.builder()
                .mtngNm(request.getMtngNm())
                .mtngTypeCd(typeCode)
                .crtrId(empId)
                .beginDt(request.getBeginDt())
                .endDt(request.getEndDt())
                .confRmId(request.getConfRmId())
                .rsrvPurps(request.getMtngNm())
                .ptcptEmpIds(request.getPtcptEmpIds())
                .build();

        return creator.create(context);
    }

    @Override
    public List<MtngListResponse> getMtngList(MtngListRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();

        List<MtngListVO> mtngListVOList = mtngMapper.selectMtngList(
                empId,
                request.getKeyword(),
                request.getBeginDt(),
                request.getEndDt()
        );

        LocalDateTime now = LocalDateTime.now();

        return mtngListVOList.stream()
                .map(vo -> mtngDtoMapper.toMtngListResponse(vo, now))
                .toList();
    }

    @Override
    public MtngDetailResponse getMtngDetail(Long mtngId) {
        MtngDetailVO detailVO = mtngMapper.selectMtngDetail(mtngId);
        if (detailVO == null) {
            throw new CustomException(ErrorCode.MTNG_NOT_FOUND);
        }

        List<MtngPtcptDetailVO> ptcptDetailVOList = mtngMapper.selectMtngPtcptDetailList(mtngId);

        List<MtngPtcptResponse> ptcptList = mtngDtoMapper.toPtcptResponseList(ptcptDetailVOList);

        return toMtngDetailResponse(detailVO, ptcptList, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void updateMtng(Long mtngId, MtngUpdateRequest request) {
        // 1. 회의 존재 + 시작 전 여부 검증
        LocalDateTime beginDt = mtngMapper.selectMtngBeginDt(mtngId);
        if (beginDt == null) {
            throw new CustomException(ErrorCode.MTNG_NOT_FOUND);
        }
        if (LocalDateTime.now().isAfter(beginDt)) {
            throw new CustomException(ErrorCode.MTNG_ALREADY_STARTED);
        }

        // 2. 요청 타입 검증
        MtngTypeCode newTypeCode = MtngTypeCode.fromCode(request.getMtngTypeCd());

        // 3. 현재 화상회의 존재 여부 확인 (타입 변경 분기 판단용)
        VideoConfVO currentVideoConf = mtngMapper.selectVideoConfByMtngId(mtngId);
        boolean hadVideoConf = currentVideoConf != null;
        boolean needsVideoConf = (newTypeCode == MtngTypeCode.ONLINE || newTypeCode == MtngTypeCode.HYBRID);

        // 4. TB_MTNG 기본정보 UPDATE
        MtngVO mtngVO = MtngVO.builder()
                .mtngId(mtngId)
                .mtngNm(request.getMtngNm())
                .mtngTypeCd(newTypeCode.getCode())
                .beginDt(request.getBeginDt())
                .endDt(request.getEndDt())
                .build();

        // 5. 회의실 연결 처리 (해제 -> 변경 -> 신규 모두 covering)
        updateConfRmRsrv(mtngVO, request);

        mtngMapper.updateMtng(mtngVO);

        // 6. 참여자 전체 삭제 후 재등록
        mtngMapper.deleteMtngPtcptByMtngId(mtngId);
        List<MtngPtcptVO> ptcptVOList = request.getPtcptEmpIds().stream()
                .map(empId -> MtngPtcptVO.builder()
                        .mtngId(mtngId)
                        .empId(empId)
                        .build())
                .toList();
        mtngMapper.createMtngPtcptList(ptcptVOList);

        // 7. 화상회의 유무 변경 처리
        if (hadVideoConf && !needsVideoConf) {
            mtngMapper.deleteVideoConfByMtngId(mtngId);
        } else if (!hadVideoConf && needsVideoConf) {
            createVideoConf(mtngId, request);
        }
    }

    // 회의실 연결 변경 처리: 새 예약 생성(중복체크 포함) 또는 미사용으로 전환
    private void updateConfRmRsrv(MtngVO mtngVO, MtngUpdateRequest request) {
        if (request.getConfRmId() == null) {
            mtngVO.setConfRmRsrvId(null);
            return;
        }

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                request.getConfRmId(),
                request.getMtngNm(),
                "N",
                request.getBeginDt(),
                request.getEndDt()
        );

        Long rsrvId = reservationService.createReservation(reservationRequest);
        mtngVO.setConfRmRsrvId(rsrvId);
    }

    // 화상회의 신규 생성 (OFFLINE -> ONLINE/HYBRID 전환 시)
    private void createVideoConf(Long mtngId, MtngUpdateRequest request) {
        VideoConfVO videoConfVO = VideoConfVO.builder()
                .mtngId(mtngId)
                .roomNm("temp")
                .vconfNm(request.getMtngNm())
                .crtrId(SecurityUtil.getCurrentEmpId())
                .creatDt(LocalDateTime.now())
                .build();

        videoConfMapper.createVideoConf(videoConfVO);

        String roomNm = "meeting-" + videoConfVO.getVconfId();
        videoConfMapper.updateRoomNm(videoConfVO.getVconfId(), roomNm);
    }

    @Override
    @Transactional
    public void deleteMtng(Long mtngId) {
        LocalDateTime beginDt = mtngMapper.selectMtngBeginDt(mtngId);
        if (beginDt == null) {
            throw new CustomException(ErrorCode.MTNG_NOT_FOUND);
        }
        if (LocalDateTime.now().isAfter(beginDt)) {
            throw new CustomException(ErrorCode.MTNG_ALREADY_STARTED);
        }

        mtngMapper.deleteMtng(mtngId);
    }

    // getMtngDetail에서만 사용하는 private 헬퍼
    // MapStruct로 기본 필드 변환 후, 권한 계산값(canEdit/canDelete/canEnd)을 toBuilder로 추가
    private MtngDetailResponse toMtngDetailResponse(
            MtngDetailVO vo, List<MtngPtcptResponse> ptcptList, LocalDateTime now) {
        Long empId = SecurityUtil.getCurrentEmpId();
        MtngSttus sttus = MtngSttus.of(vo.getBeginDt(), vo.getEndDt(), now);
        boolean isMine = vo.getCrtrId().equals(empId);

        return mtngDtoMapper.toMtngDetailResponse(vo, ptcptList, now)
                .toBuilder()
                .canEdit(isMine && sttus == MtngSttus.SCHEDULED)
                .canDelete(isMine && sttus == MtngSttus.SCHEDULED)
                .canEnd(isMine && sttus == MtngSttus.LIVE)
                .build();
    }
}