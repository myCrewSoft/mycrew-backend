package com.mycrewsoft.domain.mtng.vo.mom;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MtngMomHistVO {

	private Long histId;
	private Long momId;		// TB_MTNG_MOM (변경 없음, MOM_ID는 그대로)
	private String momCn;
	private Long edtrId;
	private String edtrNm; // 수정자명 (직원 JOIN)
	private LocalDateTime editDt;
}