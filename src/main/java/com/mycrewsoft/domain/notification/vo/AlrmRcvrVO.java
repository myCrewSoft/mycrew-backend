package com.mycrewsoft.domain.notification.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlrmRcvrVO {
	
	private Long AlrmRcvrId;			// 알림 수신자 ID(TB_MEMBER) - PK
    private Long AlrmId;				// 알림 ID - FK

    private LocalDateTime AlrmCfmtnDt;	// 알림 확인 일시
    
    private String AlrmDelYn;			// 알림 삭제 여부
    private LocalDateTime ALRM_DEL_DT;	// 알림 삭제 일시
    
}
