package com.mycrewsoft.domain.employee.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleCreateRequestDTO {

    @NotBlank
    private String roleCode;

    @NotBlank
    private String roleName;

    private String description;

    @NotEmpty
    private List<Long> permissionIds;
}
