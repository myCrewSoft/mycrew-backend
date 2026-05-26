package com.mycrewsoft.security.authz;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 인가 판단에 필요한 리소스의 문맥 정보를 담는 모델.
 *
 * 역할:
 * - 권한 검사 대상 리소스가 어느 범위에 속하는지 표현한다.
 * - AuthorizationService가 scopeType과 비교할 기준 값을 제공한다.
 *
 * 예:
 * - 부서 리소스: deptCd
 * - 프로젝트 리소스: projId
 * - 본인 소유 리소스: ownerEmpId
 */
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
