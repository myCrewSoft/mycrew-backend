package com.mycrewsoft.security.authz;

public interface AuthorizationRelationshipResolver {

    boolean isSameDepartment(Long empId, String deptCd);

    boolean isProjectMember(Long empId, String projId);

    boolean isProjectLeader(Long empId, String projId);

    static AuthorizationRelationshipResolver noop() {
        return new AuthorizationRelationshipResolver() {
            @Override
            public boolean isSameDepartment(Long empId, String deptCd) {
                return false;
            }

            @Override
            public boolean isProjectMember(Long empId, String projId) {
                return false;
            }

            @Override
            public boolean isProjectLeader(Long empId, String projId) {
                return false;
            }
        };
    }
}
