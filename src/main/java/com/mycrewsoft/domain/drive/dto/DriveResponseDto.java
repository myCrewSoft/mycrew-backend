package com.mycrewsoft.domain.drive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "드라이브 응답 DTO")
public class DriveResponseDto {

    @Schema(description = "드라이브 아이템 ID")
    private Long driveItemId;

    @Schema(description = "부모 드라이브 아이템 ID")
    private Long prntDriveItemId;

    @Schema(description = "최초 등록자 ID")
    private Long frstRgtrId;

    @Schema(description = "최종 수정자 ID")
    private Long lastMdfrId;

    @Schema(description = "아이템 유형 코드 (01:폴더, 02:파일)")
    private String itemTypeCd;

    @Schema(description = "즐겨찾기 여부")
    private String bookmarkYn;

    @Schema(description = "아이템명")
    private String itemNm;

    @Schema(description = "생성일시")
    private String frstRegDt;

    @Schema(description = "수정일시")
    private String lastMdfcnDt;

    @Schema(description = "삭제 여부")
    private String delYn;

    @Schema(description = "삭제일시")
    private String delDt;

    @Schema(description = "삭제자 ID")
    private Long deltrMbrId;

    @Schema(description = "드라이브 범위 코드 (01:개인, 02:프로젝트)")
    private String driveScopeCd;

    @Schema(description = "프로젝트 ID")
    private Long projId;

    // 조회용 추가 필드
    @Schema(description = "파일 크기")
    private String fileSz;

    @Schema(description = "원본 파일명")
    private String orgnlFileNm;

    @Schema(description = "몇 분 전/몇 시간 전")
    private String timeAgo;
    
    @Schema(description = "하위 아이템 개수")
    private Integer childCnt; 
    
    @Schema(description = "삭제자 이름")
    private String deltrMbrNm;  // 삭제자 이름
    
    @Schema(description = "최초생성자 이름")
    private String frstRgtrNm;  // 생성자 이름
    
    @Schema(description = "최종수정자 이름")
    private String lastMdfrNm;  // 수정자 이름
}