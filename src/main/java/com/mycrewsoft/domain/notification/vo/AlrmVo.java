package com.mycrewsoft.domain.notification.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlrmVo {
    private Long alrmId;		// 알림 ID - PK

    private String alrmTtln;	// 알림 제목
    private String alrmTypeCd;	// 알림 타입
    private String alrmCn;		// 알림 내용
    
    private LocalDateTime alrmSndngDt;	// 알림 발송 일시
    
    // has many
    private List<AlrmRcvrVO> alrmRcvr;	// 수신자 목록
}
