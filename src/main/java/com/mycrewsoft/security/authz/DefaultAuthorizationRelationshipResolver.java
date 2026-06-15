package com.mycrewsoft.security.authz;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.project.mapper.ProjectMapper;
import com.mycrewsoft.domain.project.vo.ProjectVO;
import com.mycrewsoft.domain.projectmember.mapper.ProjectMemberMapper;
import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultAuthorizationRelationshipResolver implements AuthorizationRelationshipResolver {

    private final EmployeeMapper employeeMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMapper projectMapper;

    @Override
    public boolean isSameDepartment(Long empId, String deptCd) {
        if (empId == null || !StringUtils.hasText(deptCd)) {
            return false;
        }

        String currentDeptCd = employeeMapper.selectEmpDeptCodeByEmpId(empId);
        return same(currentDeptCd, deptCd);
    }

    @Override
    public boolean isProjectMember(Long empId, String projId) {
        Long normalizedProjId = parseLong(projId);
        if (empId == null || normalizedProjId == null) {
            return false;
        }

        List<ProjectMemberVO> members = projectMemberMapper.selectProjectMemberList(normalizedProjId);
        if (members == null) {
            return false;
        }

        return members.stream()
                .anyMatch(member -> Objects.equals(member.getEmpId(), empId));
    }

    @Override
    public boolean isProjectLeader(Long empId, String projId) {
        Long normalizedProjId = parseLong(projId);
        if (empId == null || normalizedProjId == null) {
            return false;
        }

        ProjectVO project = projectMapper.selectProject(normalizedProjId, empId);
        return project != null && Objects.equals(project.getProjLdrEmpId(), empId);
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean same(String left, String right) {
        return StringUtils.hasText(left)
                && StringUtils.hasText(right)
                && left.trim().equals(right.trim());
    }
}
