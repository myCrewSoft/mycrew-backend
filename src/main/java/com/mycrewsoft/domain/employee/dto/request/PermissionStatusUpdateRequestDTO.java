package com.mycrewsoft.domain.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PermissionStatusUpdateRequestDTO {

    @NotBlank
    private String enabled;
}
