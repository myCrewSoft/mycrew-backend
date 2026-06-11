package com.mycrewsoft.domain.department.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.department.dto.request.DepartmentCreateRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentDeleteRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentMemberAssignRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentMemberTransferRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentUpdateRequestDTO;
import com.mycrewsoft.domain.department.dto.response.AdminDepartmentMemberResponseDTO;
import com.mycrewsoft.domain.department.dto.response.AdminDepartmentResponseDTO;
import com.mycrewsoft.domain.department.dto.response.DepartmentMemberMutationResponseDTO;
import com.mycrewsoft.domain.department.mapper.AdminDepartmentMapper;
import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.roleassignment.service.RoleAssignmentService;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDepartmentServiceImpl implements AdminDepartmentService {

    private final AdminDepartmentMapper departmentMapper;
    private final AuthorizationService authorizationService;
    private final RbacAuthorizationChangeService rbacAuthorizationChangeService;
    private final RoleAssignmentService roleAssignmentService;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public AdminDepartmentResponseDTO createDepartment(DepartmentCreateRequestDTO request) {
        assertDepartmentPermission(PermissionCode.ADMIN_DEPT_CREATE, null);
        validateCreateRequest(request);

        String deptCd = StringUtils.hasText(request.getDeptCd())
                ? normalizeDepartmentCode(request.getDeptCd().trim())
                : departmentMapper.selectNextDepartmentCode();
        if (departmentMapper.countActiveDepartmentByCode(deptCd) > 0) {
            throw new CustomException(ErrorCode.DUPLICATE_DEPARTMENT_CODE);
        }

        String parentDeptCd = normalizeNullableCode(request.getParentDeptCd());
        if (parentDeptCd != null) {
            assertActiveDepartment(parentDeptCd);
        }

        Long currentEmpId = SecurityUtil.getCurrentEmpId();
        DepartmentVO department = new DepartmentVO();
        department.setDeptCd(deptCd);
        department.setPrntDeptCd(parentDeptCd);
        department.setDeptNm(request.getDeptNm().trim());
        department.setUseYn("Y");
        department.setFrstRgtrId(currentEmpId);
        department.setFrstRegDt(LocalDateTime.now());
        department.setLastMdfrId(currentEmpId);
        department.setLastMdfcnDt(LocalDateTime.now());

        departmentMapper.insertDepartment(department);
        return loadDepartment(deptCd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDepartmentResponseDTO> getDepartmentList() {
        assertDepartmentPermission(PermissionCode.ADMIN_DEPT_READ, null);
        return departmentMapper.selectDepartments();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDepartmentResponseDTO getDepartment(String deptCd) {
        String normalizedDeptCd = normalizeCode(deptCd);
        assertDepartmentPermission(PermissionCode.ADMIN_DEPT_READ, normalizedDeptCd);
        return loadDepartment(normalizedDeptCd);
    }

    @Override
    @Transactional
    public AdminDepartmentResponseDTO updateDepartment(String deptCd, DepartmentUpdateRequestDTO request) {
        String normalizedDeptCd = normalizeCode(deptCd);
        assertDepartmentPermission(PermissionCode.ADMIN_DEPT_UPDATE, normalizedDeptCd);
        validateUpdateRequest(request);
        loadDepartment(normalizedDeptCd);

        String parentDeptCd = normalizeNullableCode(request.getParentDeptCd());
        if (parentDeptCd != null) {
            assertActiveDepartment(parentDeptCd);
            if (Objects.equals(normalizedDeptCd, parentDeptCd)
                    || departmentMapper.countDescendantDepartment(normalizedDeptCd, parentDeptCd) > 0) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        DepartmentVO department = new DepartmentVO();
        department.setDeptCd(normalizedDeptCd);
        department.setPrntDeptCd(parentDeptCd);
        department.setDeptNm(request.getDeptNm().trim());
        department.setLastMdfrId(SecurityUtil.getCurrentEmpId());
        department.setLastMdfcnDt(LocalDateTime.now());

        departmentMapper.updateDepartment(department);
        return loadDepartment(normalizedDeptCd);
    }

    @Override
    @Transactional
    public void deleteDepartment(String deptCd, DepartmentDeleteRequestDTO request) {
        String normalizedDeptCd = normalizeCode(deptCd);
        assertDepartmentPermission(PermissionCode.ADMIN_DEPT_DELETE, normalizedDeptCd);
        loadDepartment(normalizedDeptCd);

        if (departmentMapper.countChildDepartments(normalizedDeptCd) > 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Long> affectedEmpIds = nullToEmpty(departmentMapper.selectEnabledEmpIdsByDeptCd(normalizedDeptCd));
        if (!affectedEmpIds.isEmpty()) {
            String replacementDeptCd = normalizeReplacementDeptCd(request, normalizedDeptCd);
            assertActiveDepartment(replacementDeptCd);
            departmentMapper.updateDepartmentForAllEmployees(normalizedDeptCd, replacementDeptCd);
        }

        departmentMapper.disableDepartment(normalizedDeptCd, SecurityUtil.getCurrentEmpId());
        if (!affectedEmpIds.isEmpty()) {
            rbacAuthorizationChangeService.refreshEmployeesPermissions(affectedEmpIds);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDepartmentMemberResponseDTO> getDepartmentMembers(String deptCd) {
        String normalizedDeptCd = normalizeCode(deptCd);
        assertDepartmentPermission(PermissionCode.ADMIN_DEPT_READ, normalizedDeptCd);
        loadDepartment(normalizedDeptCd);
        return departmentMapper.selectDepartmentMembers(normalizedDeptCd);
    }

    @Override
    @Transactional
    public DepartmentMemberMutationResponseDTO assignDepartmentMembers(
            String deptCd,
            DepartmentMemberAssignRequestDTO request) {
        String normalizedDeptCd = normalizeCode(deptCd);
        assertDepartmentPermission(PermissionCode.ADMIN_DEPT_MEMBER_MANAGE, normalizedDeptCd);
        loadDepartment(normalizedDeptCd);
        List<Long> empIds = normalizeEmpIds(request == null ? null : request.getEmpIds());
        validateEnabledEmployees(empIds);

        // 부서 변경 전, 각 사원의 현재(이전) 부서를 먼저 수집한다. (변경 후에는 알 수 없음)
        Map<Long, String> oldDeptCdByEmpId = new LinkedHashMap<>();
        for (Long empId : empIds) {
            oldDeptCdByEmpId.put(empId, employeeMapper.selectEmpDeptCodeByEmpId(empId));
        }

        departmentMapper.updateDepartmentForEmployees(normalizedDeptCd, empIds);
        // 부서 변경에 맞춰 DEPT 권한 범위를 동기화한다.
        // (이전 부서 범위는 새 부서로 재지정, 이전 부서가 없던 신규 배정은 게시판 CRUD 범위 자동 생성)
        roleAssignmentService.syncDeptScopeChange(oldDeptCdByEmpId, normalizedDeptCd);
        rbacAuthorizationChangeService.refreshEmployeesPermissions(empIds);
        return new DepartmentMemberMutationResponseDTO(null, normalizedDeptCd, empIds.size());
    }

    @Override
    @Transactional
    public DepartmentMemberMutationResponseDTO transferDepartmentMembers(
            String deptCd,
            DepartmentMemberTransferRequestDTO request) {
        String sourceDeptCd = normalizeCode(deptCd);
        assertDepartmentPermission(PermissionCode.ADMIN_DEPT_MEMBER_MANAGE, sourceDeptCd);
        if (request == null || !StringUtils.hasText(request.getTargetDeptCd())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String targetDeptCd = normalizeCode(request.getTargetDeptCd());
        if (Objects.equals(sourceDeptCd, targetDeptCd)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        loadDepartment(sourceDeptCd);
        loadDepartment(targetDeptCd);

        List<Long> empIds = normalizeEmpIds(request.getEmpIds());
        if (departmentMapper.countEmployeesByDeptAndIds(sourceDeptCd, empIds) != empIds.size()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        departmentMapper.updateDepartmentForEmployees(targetDeptCd, empIds);
        // 이동 대상은 모두 sourceDeptCd 소속임이 검증됐으므로, 이전 부서 = sourceDeptCd 로 DEPT 범위를 재지정한다.
        Map<Long, String> oldDeptCdByEmpId = new LinkedHashMap<>();
        for (Long empId : empIds) {
            oldDeptCdByEmpId.put(empId, sourceDeptCd);
        }
        roleAssignmentService.syncDeptScopeChange(oldDeptCdByEmpId, targetDeptCd);
        rbacAuthorizationChangeService.refreshEmployeesPermissions(empIds);
        return new DepartmentMemberMutationResponseDTO(sourceDeptCd, targetDeptCd, empIds.size());
    }

    private void assertDepartmentPermission(PermissionCode permissionCode, String deptCd) {
        ResourceContext.ResourceContextBuilder builder = ResourceContext.builder()
                .resourceType(ResourceType.DEPARTMENT);
        if (deptCd != null) {
            builder.deptCd(deptCd).resourceId(deptCd);
        }
        authorizationService.assertCurrentUserPermission(permissionCode, builder.build());
    }

    private AdminDepartmentResponseDTO loadDepartment(String deptCd) {
        AdminDepartmentResponseDTO department = departmentMapper.selectDepartmentByCode(deptCd);
        if (department == null) {
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
        return department;
    }

    private void assertActiveDepartment(String deptCd) {
        if (departmentMapper.countActiveDepartmentByCode(deptCd) == 0) {
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
    }

    private void validateCreateRequest(DepartmentCreateRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getDeptNm())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateUpdateRequest(DepartmentUpdateRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getDeptNm())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return normalizeDepartmentCode(code.trim());
    }

    private String normalizeNullableCode(String code) {
        return StringUtils.hasText(code) ? normalizeDepartmentCode(code.trim()) : null;
    }

    private String normalizeDepartmentCode(String code) {
        if (!code.startsWith("DEPT_")) {
            return code;
        }

        String numberPart = code.substring("DEPT_".length());
        if (!numberPart.matches("\\d{1,3}")) {
            return code;
        }

        return "DEPT_" + String.format("%03d", Integer.parseInt(numberPart));
    }

    private String normalizeReplacementDeptCd(DepartmentDeleteRequestDTO request, String deptCd) {
        if (request == null || !StringUtils.hasText(request.getReplacementDeptCd())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String replacementDeptCd = normalizeDepartmentCode(request.getReplacementDeptCd().trim());
        if (Objects.equals(deptCd, replacementDeptCd)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return replacementDeptCd;
    }

    private List<Long> normalizeEmpIds(List<Long> empIds) {
        if (empIds == null || empIds.isEmpty() || empIds.stream().anyMatch(Objects::isNull)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return empIds.stream().distinct().toList();
    }

    private void validateEnabledEmployees(List<Long> empIds) {
        if (departmentMapper.countEnabledEmployeesByIds(empIds) != empIds.size()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private List<Long> nullToEmpty(List<Long> values) {
        return values == null ? List.of() : values;
    }
}
