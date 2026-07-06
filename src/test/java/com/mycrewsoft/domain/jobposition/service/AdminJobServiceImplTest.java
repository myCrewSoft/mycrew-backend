package com.mycrewsoft.domain.jobposition.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.mycrewsoft.domain.jobgrade.service.AdminJobServiceImpl;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;

@ExtendWith(MockitoExtension.class)
class AdminJobServiceImplTest {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AdminJobMapper adminJobMapper;

    @Mock
    private RbacAuthorizationChangeService rbacAuthorizationChangeService;

    @Test
    void getRanksChecksJobManagePermissionAndReturnsRanks() {
        RankResponseDTO rank = new RankResponseDTO();
        rank.setRankId("JOB01");
        when(adminJobMapper.selectRanks()).thenReturn(List.of(rank));

        AdminJobServiceImpl service = service();

        List<RankResponseDTO> ranks = service.getRanks();

        assertThat(ranks).containsExactly(rank);
        verify(authorizationService).assertCurrentUserPermission(eq(PermissionCode.ADMIN_JOB_MANAGE), any(ResourceContext.class));
    }

    @Test
    void createRankRejectsDuplicateRankId() {
        RankCreateRequestDTO request = createRequest();
        when(adminJobMapper.selectNextRankCode()).thenReturn("JOB01");
        when(adminJobMapper.selectRankById("JOB01")).thenReturn(new RankResponseDTO());

        AdminJobServiceImpl service = service();

        assertThatThrownBy(() -> service.createRank(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_RANK_ID);

        verify(adminJobMapper, never()).insertRank(any());
    }

    @Test
    void updateRankUpdatesExistingRank() {
        RankUpdateRequestDTO request = new RankUpdateRequestDTO();
        request.setRankName("Senior Manager");
        request.setSortOrder(2);
        RankResponseDTO existing = rank("JOB01", "Manager", 1);
        RankResponseDTO updated = rank("JOB01", "Senior Manager", 2);
        when(adminJobMapper.selectRankById("JOB01")).thenReturn(existing, updated);

        AdminJobServiceImpl service = service();

        RankResponseDTO response = service.updateRank("JOB01", request);

        assertThat(response).isEqualTo(updated);
        verify(adminJobMapper).updateRank("JOB01", "Senior Manager", 2, null);
    }

    @Test
    void deleteRankWithAssignedEmployeesRequiresReplacementRank() {
        when(adminJobMapper.selectRankById("JOB01")).thenReturn(rank("JOB01", "Manager", 1));
        when(adminJobMapper.selectEmpIdsByRankId("JOB01")).thenReturn(List.of(20260001L));

        AdminJobServiceImpl service = service();

        assertThatThrownBy(() -> service.deleteRank("JOB01", new RankDeleteRequestDTO()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        verify(adminJobMapper, never()).disableRank(any(), any());
    }

    @Test
    void deleteRankTransfersEmployeesDisablesRankAndRefreshesAffectedEmployees() {
        RankDeleteRequestDTO request = new RankDeleteRequestDTO();
        request.setReplacementRankId("JOB02");
        List<Long> affectedEmpIds = List.of(20260001L, 20260002L);
        when(adminJobMapper.selectRankById("JOB01")).thenReturn(rank("JOB01", "Manager", 1));
        when(adminJobMapper.selectRankById("JOB02")).thenReturn(rank("JOB02", "Staff", 2));
        when(adminJobMapper.selectEmpIdsByRankId("JOB01")).thenReturn(affectedEmpIds);

        AdminJobServiceImpl service = service();

        service.deleteRank("JOB01", request);

        InOrder order = inOrder(adminJobMapper, rbacAuthorizationChangeService);
        order.verify(adminJobMapper).updateEmployeeRank("JOB01", "JOB02");
        order.verify(adminJobMapper).disableRank("JOB01", null);
        order.verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(affectedEmpIds);
    }

    @Test
    void assignRankUpdatesEmployeesAndRefreshesPermissions() {
        RankAssignRequestDTO request = new RankAssignRequestDTO();
        request.setEmpIds(List.of(20260001L, 20260002L));
        RankResponseDTO assignedRank = rank("JOB01", "Manager", 1);
        when(adminJobMapper.selectRankById("JOB01")).thenReturn(assignedRank);
        when(adminJobMapper.countEnabledEmployeesByIds(request.getEmpIds())).thenReturn(2);

        AdminJobServiceImpl service = service();

        RankResponseDTO response = service.assignRank("JOB01", request);

        assertThat(response).isEqualTo(assignedRank);
        verify(adminJobMapper).updateEmployeesRank("JOB01", request.getEmpIds());
        verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(request.getEmpIds());
    }

    @Test
    void assignRankRejectsMissingEmployees() {
        RankAssignRequestDTO request = new RankAssignRequestDTO();
        request.setEmpIds(List.of(20260001L, 20260002L));
        when(adminJobMapper.selectRankById("JOB01")).thenReturn(rank("JOB01", "Manager", 1));
        when(adminJobMapper.countEnabledEmployeesByIds(request.getEmpIds())).thenReturn(1);

        AdminJobServiceImpl service = service();

        assertThatThrownBy(() -> service.assignRank("JOB01", request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(adminJobMapper, never()).updateEmployeesRank(any(), any());
    }

    @Test
    void revokeRankTransfersSelectedEmployeesToReplacementRankAndRefreshesPermissions() {
        RankRevokeRequestDTO request = new RankRevokeRequestDTO();
        request.setEmpIds(List.of(20260001L, 20260002L));
        RankResponseDTO sourceRank = rank("JOB01", "Manager", 1);
        when(adminJobMapper.selectRankById("JOB01")).thenReturn(sourceRank);
        when(adminJobMapper.countEmployeesByRankAndIds("JOB01", request.getEmpIds())).thenReturn(2);

        AdminJobServiceImpl service = service();

        RankResponseDTO response = service.revokeRank("JOB01", request);

        assertThat(response).isEqualTo(sourceRank);
        verify(adminJobMapper).updateSelectedEmployeesRank("JOB01", request.getEmpIds());
        verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(request.getEmpIds());
    }

    private AdminJobServiceImpl service() {
        return new AdminJobServiceImpl(authorizationService, adminJobMapper, rbacAuthorizationChangeService);
    }

    private RankCreateRequestDTO createRequest() {
        RankCreateRequestDTO request = new RankCreateRequestDTO();
        request.setRankName("Manager");
        request.setSortOrder(1);
        return request;
    }

    private RankResponseDTO rank(String rankId, String rankName, Integer sortOrder) {
        RankResponseDTO rank = new RankResponseDTO();
        rank.setRankId(rankId);
        rank.setRankName(rankName);
        rank.setSortOrder(sortOrder);
        rank.setUseYn("Y");
        return rank;
    }
}
