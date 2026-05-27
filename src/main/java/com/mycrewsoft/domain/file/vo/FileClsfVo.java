package com.mycrewsoft.domain.file.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.Getter;

@Data
public class FileClsfVo {
	private Long atchFileId;	//첨부파일ID (PK)
	private LocalDateTime creatDt;	//생성일시
    private String atchFileBizCd;	//첨부파일업무구분 (01:게시판 첨부 파일 / 02: 드라이브 첨부파일...)
    
    // Has Many 관계 — 분류 하나에 상세 여러 개
    private List<FileDtlVo> fileDtlList;
}
