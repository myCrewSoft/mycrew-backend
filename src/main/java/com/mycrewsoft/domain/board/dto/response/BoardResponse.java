package com.mycrewsoft.domain.board.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "게시글 단건 및 목록 조회 응답 정보")
public class BoardResponse {

	@Schema(description = "게시판 ID", example = "1")
	private final Long boardId;                 // 게시판ID (BOARD_ID)
	
	@Schema(description = "게시판 유형코드", example = "NOTICE")
    private final String boardTypeCd;           // 게시판유형코드 (BOARD_TYPE_CD)
	
	@Schema(description = "게시글 제목" ,example = "2026년 상반기 전사 워크숍 일정 안내")
    private final String boardSj;               // 게시판제목 (BOARD_SJ)
	
	@Schema(description = "게시판 내용",example = "안녕하세요.총무팀 입니다.")
    private final String boardCn;               // 게시판내용 (BOARD_CN)
	
	@Schema(description = "최초 등록자 사원 ID" , example = "2")
    private final Long frstRgtrId;              // 최초등록자ID (FRST_RGTR_ID)
	
	@Schema(description = "최초 등록 일시" , example = "2026-05-28T10:22:50")
    private final LocalDateTime frstRegDt;      // 최초등록일시 (FRST_REG_DT)
    
	@Schema(description = "최종 수정 일시",example = "2026-05-28T10:49:30")
    private final LocalDateTime lastMdfrDt;     // 최종수정일시 (LAST_MDFR_DT)
    
	@Schema(description = "게시판 첨부파일 ID (첨부파일이 없을 경우 null)", example = "458")
    private final Long boardAtchFileId;         // 게시판첨부파일ID (BOARD_ATCH_FILE_ID)
    
	@Schema(description = "부서 코드", example = "DEV")
    private final String deptCd;                // 부서코드 (DEPT_CD)
    
	@Schema(description = "프로젝트 ID", example = "15")
    private final Long projId;                  // 프로젝트ID (PROJ_ID)
    
	@Schema(description = "중요 공지 여부 (Y / N)", example = "N")
    private final String imprtntYn;             // 중요여부 (IMPRTNT_YN)
    
	@Schema(description = "댓글 허용 여부 (Y / N)", example = "Y")
    private final String cmntUseYn;             // 댓글허용여부 (CMNT_USE_YN)
    
	@Schema(description = "게시글 조회수", example = "142")
    private final Integer viewCnt;              // 조회수 (VIEW_CNT)
}
