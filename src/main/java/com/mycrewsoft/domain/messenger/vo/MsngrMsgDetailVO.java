package com.mycrewsoft.domain.messenger.vo;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsngrMsgDetailVO {
    private Long msgId;
    private Long chtrmId;
    private Long sndrId;
    private String senderName; // TB_MEMBER JOIN 결과 - DB 컬럼 아님
    private String msgCn;
    private LocalDateTime creatDt;
    private Integer unreadCount; // 서브쿼리 계산값 - DB 컬럼 아님
}