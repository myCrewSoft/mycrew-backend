package com.mycrewsoft.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.domain.notification.mapper.NotificationDtoMapper;
import com.mycrewsoft.domain.notification.vo.AlrmRcvrVO;
import com.mycrewsoft.domain.notification.vo.AlrmVO;
import com.mycrewsoft.domain.notification.vo.NotificationQueryVO;

class NotificationDtoMapperTest {

    private final NotificationDtoMapper dtoMapper =
            Mappers.getMapper(NotificationDtoMapper.class);

    @Test
    void toResponse_mapsNotificationQuery() {
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 6, 13, 10, 0);
        NotificationQueryVO vo = new NotificationQueryVO();
        vo.setAlrmRcvrId(10L);
        vo.setAlrmId(101L);
        vo.setAlrmCfmtnDt(confirmedAt);

        NotificationResponse result = dtoMapper.toResponse(vo);

        assertThat(result.getAlrmRcvrId()).isEqualTo(10L);
        assertThat(result.getAlrmId()).isEqualTo(101L);
        assertThat(result.getAlrmCfmtnDt()).isEqualTo(confirmedAt);
    }

    @Test
    void toResponse_mapsInsertedReceiverIdForSse() {
        AlrmVO alrmVO = new AlrmVO();
        alrmVO.setAlrmId(101L);
        alrmVO.setAlrmTtln("테스트 알림");

        AlrmRcvrVO alrmRcvrVO = new AlrmRcvrVO();
        alrmRcvrVO.setAlrmRcvrId(10L);
        alrmRcvrVO.setAlrmId(101L);

        NotificationResponse result = dtoMapper.toResponse(alrmVO, alrmRcvrVO);

        assertThat(result.getAlrmRcvrId()).isEqualTo(10L);
        assertThat(result.getAlrmId()).isEqualTo(101L);
        assertThat(result.getAlrmTtln()).isEqualTo("테스트 알림");
    }
}
