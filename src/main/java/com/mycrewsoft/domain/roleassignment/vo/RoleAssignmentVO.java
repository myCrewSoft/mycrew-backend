package com.mycrewsoft.domain.roleassignment.vo;

import com.mycrewsoft.domain.role.vo.RoleVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleAssignmentVO {
	private Long roleAssignId;
	private Long roleId;
	private Long empId;
	private String scopeTypeCd;
	private String scopeId;
	private String enabled;

	private RoleVO role;
}
