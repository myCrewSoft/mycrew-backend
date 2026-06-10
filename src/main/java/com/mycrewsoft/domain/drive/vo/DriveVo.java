package com.mycrewsoft.domain.drive.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DriveVo {
	private Long driveItemId;
	private Long prntDriveItemId;
	private Long frstRgtrId;
	private Long lastMdfrId;
	private String itemTypeCd;
	private String bookmarkYn;
	private String itemNm;
	private LocalDateTime frstRegDt;
	private LocalDateTime lastMdfcnDt;
	private String delYn;
	private LocalDateTime delDt;
	private Long deltrMbrId;
	private Long driveAtchFileId;
	private String driveScopeCd;
	private Long projId;
	
   //조회 목적
   private Long fileSz; //파일 사이즈
   private String orgnlFileNm; //실제파일명
   private Integer childCnt; //하위파일 개수
   private String deltrMbrNm;  // 삭제자 이름
   private String frstRgtrNm;  // 생성자 이름
   private String lastMdfrNm;  // 수정자 이름
    
}
