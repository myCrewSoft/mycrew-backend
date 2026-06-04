package com.mycrewsoft.domain.messenger.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsngrChtrmListVO {
    private Long chtrmId;
    private String chtrmNm;
    private String chtrmExpln;
    private String chtrmTypeCd;
    private Long estblshId;
    private LocalDateTime creatDt;
    private LocalDateTime endDt;

    private Integer unreadCount; // 서브쿼리 계산값 - DB 컬럼 아님

    private List<MsngrChtrmPtcptVO> msngrChtrmPtcpt;
    private List<MsngrMsgVO> msngrMsg;
}