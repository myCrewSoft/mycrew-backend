package com.mycrewsoft.domain.board.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.board.vo.BoardCommentVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor; // 💡 마이바티스 객체 생성을 위해 필수 추가
import lombok.Setter;           // 💡 값 주입을 위해 추가

@Getter
@Setter                       // 💡 마이바티스가 값을 맵핑할 수 있도록 Setter 허용
@Builder
@NoArgsConstructor            // 💡 리플렉션을 위한 기본 생성자 보완
@AllArgsConstructor           // 💡 빌더 패턴과 기본 생성자를 공존시키기 위한 전체 생성자 명시
@Schema(description = "게시글 단건 및 목록 조회 응답 정보")
public class BoardResponse {

	@Schema(description = "게시판 ID", example = "1")
	private Long boardId;                
	
	@Schema(description = "게시판 유형코드", example = "NOTICE")
    private String boardTypeCd;          
	
	@Schema(description = "게시글 제목" ,example = "2026년 상반기 전사 워크숍 일정 안내")
    private String boardSj;              
	
	@Schema(description = "게시판 내용",example = "안녕하세요.총무팀 입니다.")
    private String boardCn;              
	
	@Schema(description = "최초 등록자 사원 ID" , example = "2")
    private Long frstRgtrId;             
	@Schema(description = "최초 등록 일시" , example = "2026-05-28T10:22:50")
    private LocalDateTime frstRegDt;      
    
	@Schema(description = "최종 수정 일시",example = "2026-05-28T10:49:30")
    private LocalDateTime lastMdfrDt;    
    
	@Schema(description = "게시판 첨부파일 ID (첨부파일이 없을 경우 null)", example = "458")
    private Long boardAtchFileId;        
    
	@Schema(description = "부서 코드", example = "DEPT")
    private String deptCd;                
    
	@Schema(description = "프로젝트 ID", example = "15")
    private Long projId;
	
	
	@Schema(description = "프로젝트명", example = "마이크루소프트 차세대 시스템 구축")
	private String projNm;

	@Schema(description = "프로젝트 상태코드", example = "01")
	private String projStatCd;
    
	@Schema(description = "중요 공지 여부 (Y / N)", example = "N")
    private String imprtntYn;         
    
	@Schema(description = "댓글 허용 여부 (Y / N)", example = "Y")
    private String cmntUseYn;            
    
	@Schema(description = "게시글 조회수", example = "142")
    private Integer viewCnt;     
	
	@Schema(description = "댓글 목록")
    private List<BoardCommentVO> commentList; 
    
	@Schema(description = "게시글 총 좋아요 수", example = "10")
	private Integer likeCnt;
	
    @Schema(description = "현재 사용자 좋아요 여부")
    private Boolean isLiked;
}