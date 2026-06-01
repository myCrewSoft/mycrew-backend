package com.mycrewsoft.domain.role.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleVO {

	private Long roleId;
	private String roleCd;
	private String roleNm;
	private String roleExpln;
	private Long frstRgtrId;
	private LocalDateTime frstRegDt;
	private Long lastMdfrId;
	private LocalDateTime lastMdfcnDt;
}
