package com.mycrewsoft.security.authz;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceContext {

    private ResourceType resourceType;
    private String resourceId;
    private String deptCd;
    private String projId;
    private Long ownerEmpId;
    private Long managerEmpId;

    @Builder.Default
    private Set<Long> memberEmpIds = new LinkedHashSet<>();
}
