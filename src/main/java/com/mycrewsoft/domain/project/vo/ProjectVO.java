package com.mycrewsoft.domain.project.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;
import com.mycrewsoft.domain.projectreport.vo.ProjectReportVO;
import com.mycrewsoft.domain.task.vo.TaskVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectVO {
	private Long projId;					//프로젝트 아이디 (PK)
	private Long chtrmId;					//채팅방 아이디
	private String projNm;					//프로젝트명
	private String projCn;					//프로젝트 상세내용
	private LocalDate projBgngYmd;			//프로젝트 시작날짜
	private LocalDate projEndYmd;			//프로젝트 종료날짜
	private String projStatCd;				//프로젝트 상태코드 : 01(예정) / 02(진행 중) / 03(완료) / 04(중단)
	private LocalDateTime projCreatDt;		//프로젝트 생성날짜
	private LocalDateTime projMdfcnDt;		//프로젝트 수정날짜
	private LocalDateTime projStatChgDt;	//프로젝트 상태코드 변경날짜
	private Long projLdrEmpId;				//프로젝트 리더 아이디
	private Long projAtchFileId;			//프로젝트 첨부파일 아이디
	private Integer projPrgrsRt;			//프로젝트 진척률
	
	// Has
	private List<TaskVO> taskList;	
	private List<ProjectMemberVO> projMemberList;
	private List<ProjectReportVO> projReportList;
	
	// 조회용 JOIN 필드
	private String projLdrNm;    // 프로젝트 장 이름
	private String empNm;
	private String deptNm;
}
