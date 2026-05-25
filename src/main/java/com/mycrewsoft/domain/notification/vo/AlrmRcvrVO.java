package com.mycrewsoft.domain.notification.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlrmRcvrVO {
	
	private Long alrmRcvrId;			// 알림 수신자 ID(TB_MEMBER) - PK
    private Long alrmId;				// 알림 ID - FK

    private LocalDateTime alrmCfmtnDt;	// 알림 확인 일시
    
    private String alrmDelYn;			// 알림 삭제 여부
    private LocalDateTime alrmDelDt;	// 알림 삭제 일시
    
}
