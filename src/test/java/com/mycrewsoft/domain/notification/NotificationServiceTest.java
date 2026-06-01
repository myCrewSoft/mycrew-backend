package com.mycrewsoft.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.domain.notification.dto.response.NotificationUnreadCountResponse;
import com.mycrewsoft.domain.notification.mapper.NotificationDtoMapper;
import com.mycrewsoft.domain.notification.mapper.NotificationMapper;
import com.mycrewsoft.domain.notification.service.NotificationServiceImpl;
import com.mycrewsoft.domain.notification.service.SseEmitterService;
import com.mycrewsoft.domain.notification.vo.AlrmVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private SseEmitterService sseEmitterService;

    @Mock
    private NotificationDtoMapper dtoMapper;

    @Test
    @DisplayName("알림 목록 조회 성공")
    void readAlrmList_success() {
        Long empId = 1L;
        List<AlrmVO> voList = List.of(new AlrmVO(), new AlrmVO());
        List<NotificationResponse> responseList = List.of(
                NotificationResponse.builder().alrmId(1L).alrmTtln("테스트1").build(),
                NotificationResponse.builder().alrmId(2L).alrmTtln("테스트2").build()
        );

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            given(notificationMapper.selectAlrmList(empId)).willReturn(voList);
            given(dtoMapper.toResponseList(voList)).willReturn(responseList);

            List<NotificationResponse> result = notificationService.readAlrmList();

            assertThat(result).hasSize(2);
            verify(notificationMapper, times(1)).selectAlrmList(empId);
        }
    }

    @Test
    @DisplayName("미읽음 알림 개수 조회 성공")
    void readUnreadCount_success() {
        Long empId = 1L;

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            given(notificationMapper.selectUnreadCount(empId)).willReturn(3L);

            NotificationUnreadCountResponse result = notificationService.readUnreadCount();

            assertThat(result.getUnreadCount()).isEqualTo(3L);
        }
    }

    @Test
    @DisplayName("알림 발송 성공 - 수신자 2명")
    void sendAlrm_success() {
        AlrmVO alrmVO = new AlrmVO();
        alrmVO.setAlrmTtln("테스트 알림");
        alrmVO.setAlrmTypeCd("01");
        alrmVO.setAlrmCn("알림 내용");
        List<Long> rcvrEmpIds = List.of(1L, 2L);

        notificationService.sendAlrm(alrmVO, rcvrEmpIds);

        verify(notificationMapper, times(1)).insertAlrm(alrmVO);
        verify(notificationMapper, times(2)).insertAlrmRcvr(any());
        verify(sseEmitterService, times(2)).send(anyLong(), any());
    }

    @Test
    @DisplayName("알림 삭제 성공")
    void deleteAlrm_success() {
        Long empId = 1L;
        Long alrmRcvrId = 10L;

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            given(notificationMapper.updateDelYn(alrmRcvrId, empId)).willReturn(1);

            notificationService.deleteAlrm(alrmRcvrId);

            verify(notificationMapper, times(1)).updateDelYn(alrmRcvrId, empId);
        }
    }

    @Test
    @DisplayName("알림 삭제 실패 - 본인 알림 아님")
    void deleteAlrm_fail_notFound() {
        Long empId = 1L;
        Long alrmRcvrId = 10L;

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            given(notificationMapper.updateDelYn(alrmRcvrId, empId)).willReturn(0);

            assertThatThrownBy(() -> notificationService.deleteAlrm(alrmRcvrId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }
}