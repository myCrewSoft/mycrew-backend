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
    private Long chtrmImgAtchFileId;
    private LocalDateTime creatDt;
    private LocalDateTime endDt;

    private String lastMessage; // 마지막 메시지 내용 - DB 컬럼 아님
    private String lastTime; // 마지막 메시지 시간 - DB 컬럼 아님
    private Integer unreadCount; // 서브쿼리 계산값 - DB 컬럼 아님
    private Long prflImgFileId; // 1:1 상대 프로필 이미지 첨부파일 ID - DB 컬럼 아님
    private String status; // 1:1 상대 상태 - DB 컬럼 아님
    private String jobTitle; // 1:1 상대 직책명 - DB 컬럼 아님
    private String department; // 1:1 상대 부서명 - DB 컬럼 아님
    private Integer participantCount; // 현재 참여자 수 - DB 컬럼 아님

    private List<MsngrChtrmPtcptVO> msngrChtrmPtcpt;
}
