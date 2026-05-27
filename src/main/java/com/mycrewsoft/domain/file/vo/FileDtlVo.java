package com.mycrewsoft.domain.file.vo;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class FileDtlVo {
	private Long atchFileDtlId;			//첨부파일상세ID(PK)
    private Long atchFileId;			//첨부파일ID
    private String orgnlFileNm;			//원본파일명
    private String atchFileTyCd;   		//첨부파일타입코드 (01:IMAGE / 02:DOCUMENT)
    private String savePathNm;     		//저장경로
    private String saveFileNm;     		//저장파일명 (UUID 기반)
    private String fileExtsn;      		//파일확장자
    private Long fileSz;           		//파일사이즈
    private String fileCn;         		//파일내용 (파일 세부 설명)
    private LocalDateTime frstRegstDt; 	//최초등록일시
    private Long frstRgstrId;			//최조등록자ID
    private String delYn;          		//삭제여부 (default : N)
    private LocalDateTime delDt;		//삭제일시
    private Long dltrsId;				//삭제자ID
    
}
