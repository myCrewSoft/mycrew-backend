package com.mycrewsoft.domain.employee.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminScopeOptionResponseDTO {

    private String scopeId;
    private String scopeCode;
    private String scopeName;
    private String label;

    public AdminScopeOptionResponseDTO(String scopeId, String scopeCode, String scopeName) {
        this.scopeId = scopeId;
        this.scopeCode = scopeCode;
        this.scopeName = scopeName;
        this.label = scopeCode + " - " + scopeName;
    }
}
