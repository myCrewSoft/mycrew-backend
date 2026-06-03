package com.mycrewsoft.domain.jobgrade.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.jobgrade.dto.request.RankAssignRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankCreateRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankDeleteRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankRevokeRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankUpdateRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.response.RankResponseDTO;
import com.mycrewsoft.domain.jobgrade.mapper.AdminJobMapper;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminJobServiceImpl implements AdminJobService {

    private final AuthorizationService authorizationService;
    private final AdminJobMapper adminJobMapper;
    private final RbacAuthorizationChangeService rbacAuthorizationChangeService;

    @Override
    @Transactional(readOnly = true)
    public List<RankResponseDTO> getRanks() {
        assertJobManagePermission();
        return adminJobMapper.selectRanks();
    }

    @Override
    @Transactional
    public RankResponseDTO createRank(RankCreateRequestDTO request) {
        assertJobManagePermission();
        validateCreateRequest(request);
        String rankId = normalizeId(request.getRankId());
        if (adminJobMapper.selectRankById(rankId) != null) {
            throw new CustomException(ErrorCode.DUPLICATE_RANK_ID);
        }

        JobGradeVO rank = new JobGradeVO();
        rank.setJobGrdCd(rankId);
        rank.setJobGrdNm(request.getRankName().trim());
        rank.setSortOrder(request.getSortOrder());
        rank.setUseYn("Y");
        adminJobMapper.insertRank(rank);
        return loadRank(rankId);
    }

    @Override
    @Transactional
    public RankResponseDTO updateRank(String rankId, RankUpdateRequestDTO request) {
        assertJobManagePermission();
        validateUpdateRequest(request);
        String normalizedRankId = normalizeId(rankId);
        loadRank(normalizedRankId);

        adminJobMapper.updateRank(normalizedRankId, request.getRankName().trim(), request.getSortOrder(), null);
        return loadRank(normalizedRankId);
    }

    @Override
    @Transactional
    public void deleteRank(String rankId, RankDeleteRequestDTO request) {
        assertJobManagePermission();
        String normalizedRankId = normalizeId(rankId);
        loadRank(normalizedRankId);
        List<Long> affectedEmpIds = nullToEmpty(adminJobMapper.selectEmpIdsByRankId(normalizedRankId));

        if (!affectedEmpIds.isEmpty()) {
            String replacementRankId = normalizeReplacementRankId(request, normalizedRankId);
            loadRank(replacementRankId);
            adminJobMapper.updateEmployeeRank(normalizedRankId, replacementRankId);
        }

        adminJobMapper.disableRank(normalizedRankId, null);
        if (!affectedEmpIds.isEmpty()) {
            rbacAuthorizationChangeService.refreshEmployeesPermissions(affectedEmpIds);
        }
    }

    @Override
    @Transactional
    public RankResponseDTO assignRank(String rankId, RankAssignRequestDTO request) {
        assertJobManagePermission();
        String normalizedRankId = normalizeId(rankId);
        RankResponseDTO rank = loadRank(normalizedRankId);
        List<Long> empIds = normalizeEmpIds(request == null ? null : request.getEmpIds());
        validateEnabledEmployees(empIds);

        adminJobMapper.updateEmployeesRank(normalizedRankId, empIds);
        rbacAuthorizationChangeService.refreshEmployeesPermissions(empIds);
        return rank;
    }

    @Override
    @Transactional
    public RankResponseDTO revokeRank(String rankId, RankRevokeRequestDTO request) {
        assertJobManagePermission();
        String normalizedRankId = normalizeId(rankId);
        RankResponseDTO rank = loadRank(normalizedRankId);
        List<Long> empIds = normalizeEmpIds(request == null ? null : request.getEmpIds());
        

        if (adminJobMapper.countEmployeesByRankAndIds(normalizedRankId, empIds) != empIds.size()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        adminJobMapper.updateSelectedEmployeesRank(normalizedRankId, empIds);
        rbacAuthorizationChangeService.refreshEmployeesPermissions(empIds);
        return rank;
    }

    private void assertJobManagePermission() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.ADMIN)
                .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.ADMIN_JOB_MANAGE, resource);
    }

    private RankResponseDTO loadRank(String rankId) {
        RankResponseDTO rank = adminJobMapper.selectRankById(rankId);
        if (rank == null) {
            throw new CustomException(ErrorCode.RANK_NOT_FOUND);
        }
        return rank;
    }

    private void validateCreateRequest(RankCreateRequestDTO request) {
        if (request == null
                || !StringUtils.hasText(request.getRankId())
                || !StringUtils.hasText(request.getRankName())
                || request.getSortOrder() == null
                || request.getSortOrder() < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateUpdateRequest(RankUpdateRequestDTO request) {
        if (request == null
                || !StringUtils.hasText(request.getRankName())
                || request.getSortOrder() == null
                || request.getSortOrder() < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalizeId(String rankId) {
        if (!StringUtils.hasText(rankId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return rankId.trim();
    }

    private String normalizeReplacementRankId(RankDeleteRequestDTO request, String rankId) {
        return normalizeReplacementRankId(request == null ? null : request.getReplacementRankId(), rankId);
    }

    private String normalizeReplacementRankId(String replacementRankIdValue, String rankId) {
        if (!StringUtils.hasText(replacementRankIdValue)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String replacementRankId = replacementRankIdValue.trim();
        if (Objects.equals(rankId, replacementRankId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return replacementRankId;
    }

    private List<Long> normalizeEmpIds(List<Long> empIds) {
        if (empIds == null || empIds.isEmpty() || empIds.stream().anyMatch(Objects::isNull)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return empIds.stream().distinct().toList();
    }

    private void validateEnabledEmployees(List<Long> empIds) {
        if (adminJobMapper.countEnabledEmployeesByIds(empIds) != empIds.size()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private List<Long> nullToEmpty(List<Long> values) {
        return values == null ? List.of() : values;
    }
}
