package com.mycrewsoft.domain.drive.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.Getter;

@Data
public class DriveVo {
	private Long driveItemId;        // 드라이브아이템ID (PK)
    private Long prntDriveItemId;    // 상위드라이브아이템ID (부모 폴더)
    private Long empId;              // 사원ID
    private String itemTypeCd;       // 아이템유형 (01:폴더 / 02:파일)
    private String bookmarkYn;       // 즐겨찾기여부
    private String itemNm;           // 폴더명
    private LocalDateTime frstRegDt; // 최초등록일시
    private LocalDateTime lastMdfcnDt; // 최종수정일시
    private String delYn;            // 삭제여부
    private LocalDateTime delDt;     // 삭제일시
    private Long deltrMbrId;         // 삭제자ID
    private Long driveAtchFileId;    // 드라이브첨부파일ID (논리FK)

    // Has Many 관계 — 폴더는 하위 아이템을 가짐 (폴더 안에 폴더/파일)
    private List<DriveVo> children;
}
