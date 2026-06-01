package com.mycrewsoft.domain.schedule.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchdSearchVO {

	private Long empId;
	
	private String deptCd;

    private List<String> projIds;

    private List<String> taskIds;

    private Boolean execYn;

    private LocalDateTime beginDt;

    private LocalDateTime endDt;

}
