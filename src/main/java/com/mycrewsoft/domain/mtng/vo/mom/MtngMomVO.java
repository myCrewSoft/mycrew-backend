package com.mycrewsoft.domain.mtng.vo.mom;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MtngMomVO {

	private Long momId;			// 회의록 ID - PK
	private Long mtngId;			// 회의 ID (TB_MTNG) - FK, 기존 vconfId 대체

	private String momCn;			// 회의록 내용

	private LocalDateTime creatDt;
	private LocalDateTime delDt;

	private String momSttusCd;	// 회의록 상태
	private Long edtrId;		// 담당자ID

	private LocalDateTime revwReqDt;	// 검토 요청 발송 일시
	private LocalDateTime cnfrmDt;		// 정식 등록 일시

	private Integer aprvlRoundNo;		// 결재 회차

	// 전자결재 연동
	private Long drftDocSn;	// TB_APRVL_DOC.DRFT_DOC_SN (논리FK), 결재 요청 전에는 NULL

	// has many
	private List<MtngMomHistVO> mtngMomHist;	// 수정 이력 목록

}