package com.mycrewsoft.domain.employee.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleDetailResponseDTO {

    private Long roleId;
    private String roleCode;
    private String roleName;
    private String description;
    private List<PermissionResponseDTO> permissions;
    private List<RoleEmployeeResponseDTO> employees;
}
