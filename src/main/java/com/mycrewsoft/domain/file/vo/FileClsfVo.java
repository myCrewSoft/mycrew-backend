package com.mycrewsoft.domain.file.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.Getter;

@Data
public class FileClsfVo {
	private Long atchFileId;	//첨부파일ID (PK)
	private LocalDateTime creatDt;	//생성일시
    private String useYn;	//사용여부
    
    // Has Many 관계 — 분류 하나에 상세 여러 개
    private List<FileDtlVo> fileDtlList;
}
