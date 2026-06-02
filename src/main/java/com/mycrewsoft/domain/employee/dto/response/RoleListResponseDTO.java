package com.mycrewsoft.domain.employee.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleListResponseDTO {

    private Long roleId;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer permissionCount;
    private Integer assignedEmployeeCount;
}
