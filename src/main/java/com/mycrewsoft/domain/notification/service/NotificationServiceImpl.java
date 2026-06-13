package com.mycrewsoft.domain.notification.service;

import java.util.List;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.domain.notification.dto.response.NotificationUnreadCountResponse;
import com.mycrewsoft.domain.notification.mapper.NotificationDtoMapper;
import com.mycrewsoft.domain.notification.mapper.NotificationMapper;
import com.mycrewsoft.domain.notification.vo.AlrmRcvrVO;
import com.mycrewsoft.domain.notification.vo.AlrmVO;
import com.mycrewsoft.domain.notification.vo.NotificationQueryVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationDtoMapper dtoMapper;
    private final SseEmitterService sseEmitterService;

    // 알림 조회
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> readAlrmList() {

        // 사용자 정보 조회
        Long empId = SecurityUtil.getCurrentEmpId();

        // 알림 조회
        List<NotificationQueryVO> notificationList = notificationMapper.selectAlrmList(empId);

        // VO -> DTO 변환
        return dtoMapper.toResponseList(notificationList);
    }

    // 안 읽은 알림 개수 구하기
    @Override
    @Transactional(readOnly = true)
    public NotificationUnreadCountResponse readUnreadCount() {

        // 사용자 정보 조회
        Long rcvrEmpId = SecurityUtil.getCurrentEmpId();

        // 안 읽은 알림 개수 구하기
        long count = notificationMapper.selectUnreadCount(rcvrEmpId);

        // 응답 DTO 생성
        NotificationUnreadCountResponse countResponse = 
                NotificationUnreadCountResponse.builder().unreadCount(count).build();

        return countResponse;
    }

    // 알림 발송
    @Override
    @Transactional
    public void sendAlrm(AlrmVO alrmVO, List<Long> rcvrEmpIds) {

        // 알림 생성
        alrmVO.setAlrmSndngDt(LocalDateTime.now());
        notificationMapper.insertAlrm(alrmVO);

        // 알림 대상자들에게 알림 발송
        for (Long rcvrEmpId : rcvrEmpIds) {
            if (rcvrEmpId == null) {
                continue;
            }
            AlrmRcvrVO rcvrVO = new AlrmRcvrVO();
            rcvrVO.setAlrmId(alrmVO.getAlrmId());
            rcvrVO.setRcvrEmpId(rcvrEmpId);
            notificationMapper.insertAlrmRcvr(rcvrVO);
            NotificationResponse notificationResponse = dtoMapper.toResponse(alrmVO, rcvrVO);

            sseEmitterService.send(rcvrEmpId, notificationResponse);
        }
    }

    // 알림 발송 메서드
    @Override
    @Transactional
    public void sendAlrm(String ttln, String typeCd, String cn, List<Long> rcvrEmpIds) {
        // null 체크
        if (StringUtils.isBlank(ttln)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (StringUtils.isBlank(typeCd)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (rcvrEmpIds == null || rcvrEmpIds.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // vo 생성
        AlrmVO alrmVO = new AlrmVO();
        alrmVO.setAlrmTtln(ttln);
        alrmVO.setAlrmTypeCd(typeCd);
        alrmVO.setAlrmCn(cn);

        // 알림 발송
        sendAlrm(alrmVO, rcvrEmpIds);
    }

    // 모든 알림 읽기
    @Override
    @Transactional
    public void readAllAlrm() {

        // 사용자 정보 조회
        Long rcvrEmpId = SecurityUtil.getCurrentEmpId();

        // 알림 읽기
        notificationMapper.updateReadAll(rcvrEmpId);
    }

    // 알림 삭제(논리 삭제)
    @Override
    @Transactional
    public void deleteAlrm(Long alrmRcvrId) {

        // 사용자 정보 조회
        Long rcvrEmpId = SecurityUtil.getCurrentEmpId();

        // 알림 삭제
        int result = notificationMapper.updateDelYn(alrmRcvrId, rcvrEmpId);
        if (result == 0) throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
