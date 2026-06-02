package com.mycrewsoft.domain.employee.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleAssignRequestDTO {

    @NotEmpty
    private List<Long> empIds;

    @NotBlank
    private String scopeTypeCd;

    private String scopeId;
}
