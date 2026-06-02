package com.mycrewsoft.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.notification.mapper.NotificationMapper;
import com.mycrewsoft.domain.notification.vo.AlrmRcvrVO;
import com.mycrewsoft.domain.notification.vo.AlrmVO;

@SpringBootTest
@Transactional
class NotificationSendIntegrationTest {

    @Autowired
    private NotificationMapper notificationMapper;

    private static final Long TEST_EMP_ID = 1L;

    @Test
    @DisplayName("알림 INSERT 후 목록 조회 성공")
    void insertAndSelectAlrm_success() {
        AlrmVO alrmVO = new AlrmVO();
        alrmVO.setAlrmTtln("통합 테스트 알림");
        alrmVO.setAlrmTypeCd("01");
        alrmVO.setAlrmCn("통합 테스트 알림 내용");
        notificationMapper.insertAlrm(alrmVO);

        AlrmRcvrVO rcvrVO = new AlrmRcvrVO();
        rcvrVO.setAlrmId(alrmVO.getAlrmId());
        rcvrVO.setRcvrEmpId(TEST_EMP_ID);
        notificationMapper.insertAlrmRcvr(rcvrVO);

        List<AlrmVO> result = notificationMapper.selectAlrmList(TEST_EMP_ID);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getAlrmTtln()).isEqualTo("통합 테스트 알림");
        assertThat(result.get(0).getAlrmRcvr()).isNotEmpty();
        assertThat(result.get(0).getAlrmRcvr().get(0).getRcvrEmpId()).isEqualTo(TEST_EMP_ID);
    }

    @Test
    @DisplayName("전체 읽음 처리 후 미읽음 개수 0 확인")
    void readAll_unreadCountZero() {
        AlrmVO alrmVO = new AlrmVO();
        alrmVO.setAlrmTtln("읽음 테스트 알림");
        alrmVO.setAlrmTypeCd("01");
        alrmVO.setAlrmCn("읽음 테스트 알림 내용");
        notificationMapper.insertAlrm(alrmVO);

        AlrmRcvrVO rcvrVO = new AlrmRcvrVO();
        rcvrVO.setAlrmId(alrmVO.getAlrmId());
        rcvrVO.setRcvrEmpId(TEST_EMP_ID);
        notificationMapper.insertAlrmRcvr(rcvrVO);

        long beforeCount = notificationMapper.selectUnreadCount(TEST_EMP_ID);
        assertThat(beforeCount).isPositive();

        notificationMapper.updateReadAll(TEST_EMP_ID);

        long afterCount = notificationMapper.selectUnreadCount(TEST_EMP_ID);
        assertThat(afterCount).isZero();
    }
}