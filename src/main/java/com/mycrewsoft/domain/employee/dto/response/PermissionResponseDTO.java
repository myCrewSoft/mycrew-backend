package com.mycrewsoft.domain.employee.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PermissionResponseDTO {

    private Long permissionId;
    private String permissionCode;
    private String permissionName;
    private String description;
    private String enabled;
}
