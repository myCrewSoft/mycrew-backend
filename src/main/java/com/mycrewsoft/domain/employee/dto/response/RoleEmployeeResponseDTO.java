package com.mycrewsoft.domain.employee.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleEmployeeResponseDTO {

    private Long roleAssignmentId;
    private Long empId;
    private String employeeName;
    private String deptCd;
    private String deptName;
    private String scopeTypeCd;
    private String scopeId;
    private String enabled;
}
