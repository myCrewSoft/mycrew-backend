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
        rank.setRankId("JG001");
        when(adminJobMapper.selectRanks()).thenReturn(List.of(rank));

        AdminJobServiceImpl service = service();

        List<RankResponseDTO> ranks = service.getRanks();

        assertThat(ranks).containsExactly(rank);
        verify(authorizationService).assertCurrentUserPermission(eq(PermissionCode.ADMIN_JOB_MANAGE), any(ResourceContext.class));
    }

    @Test
    void createRankRejectsDuplicateRankId() {
        RankCreateRequestDTO request = createRequest();
        when(adminJobMapper.selectRankById("JG001")).thenReturn(new RankResponseDTO());

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
        RankResponseDTO existing = rank("JG001", "Manager", 1);
        RankResponseDTO updated = rank("JG001", "Senior Manager", 2);
        when(adminJobMapper.selectRankById("JG001")).thenReturn(existing, updated);

        AdminJobServiceImpl service = service();

        RankResponseDTO response = service.updateRank("JG001", request);

        assertThat(response).isEqualTo(updated);
        verify(adminJobMapper).updateRank("JG001", "Senior Manager", 2, null);
    }

    @Test
    void deleteRankWithAssignedEmployeesRequiresReplacementRank() {
        when(adminJobMapper.selectRankById("JG001")).thenReturn(rank("JG001", "Manager", 1));
        when(adminJobMapper.selectEmpIdsByRankId("JG001")).thenReturn(List.of(20260001L));

        AdminJobServiceImpl service = service();

        assertThatThrownBy(() -> service.deleteRank("JG001", new RankDeleteRequestDTO()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        verify(adminJobMapper, never()).disableRank(any(), any());
    }

    @Test
    void deleteRankTransfersEmployeesDisablesRankAndRefreshesAffectedEmployees() {
        RankDeleteRequestDTO request = new RankDeleteRequestDTO();
        request.setReplacementRankId("JG002");
        List<Long> affectedEmpIds = List.of(20260001L, 20260002L);
        when(adminJobMapper.selectRankById("JG001")).thenReturn(rank("JG001", "Manager", 1));
        when(adminJobMapper.selectRankById("JG002")).thenReturn(rank("JG002", "Staff", 2));
        when(adminJobMapper.selectEmpIdsByRankId("JG001")).thenReturn(affectedEmpIds);

        AdminJobServiceImpl service = service();

        service.deleteRank("JG001", request);

        InOrder order = inOrder(adminJobMapper, rbacAuthorizationChangeService);
        order.verify(adminJobMapper).updateEmployeeRank("JG001", "JG002");
        order.verify(adminJobMapper).disableRank("JG001", null);
        order.verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(affectedEmpIds);
    }

    @Test
    void assignRankUpdatesEmployeesAndRefreshesPermissions() {
        RankAssignRequestDTO request = new RankAssignRequestDTO();
        request.setEmpIds(List.of(20260001L, 20260002L));
        RankResponseDTO assignedRank = rank("JG001", "Manager", 1);
        when(adminJobMapper.selectRankById("JG001")).thenReturn(assignedRank);
        when(adminJobMapper.countEnabledEmployeesByIds(request.getEmpIds())).thenReturn(2);

        AdminJobServiceImpl service = service();

        RankResponseDTO response = service.assignRank("JG001", request);

        assertThat(response).isEqualTo(assignedRank);
        verify(adminJobMapper).updateEmployeesRank("JG001", request.getEmpIds());
        verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(request.getEmpIds());
    }

    @Test
    void assignRankRejectsMissingEmployees() {
        RankAssignRequestDTO request = new RankAssignRequestDTO();
        request.setEmpIds(List.of(20260001L, 20260002L));
        when(adminJobMapper.selectRankById("JG001")).thenReturn(rank("JG001", "Manager", 1));
        when(adminJobMapper.countEnabledEmployeesByIds(request.getEmpIds())).thenReturn(1);

        AdminJobServiceImpl service = service();

        assertThatThrownBy(() -> service.assignRank("JG001", request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(adminJobMapper, never()).updateEmployeesRank(any(), any());
    }

    @Test
    void revokeRankTransfersSelectedEmployeesToReplacementRankAndRefreshesPermissions() {
        RankRevokeRequestDTO request = new RankRevokeRequestDTO();
        request.setEmpIds(List.of(20260001L, 20260002L));
        RankResponseDTO sourceRank = rank("JG001", "Manager", 1);
        when(adminJobMapper.selectRankById("JG001")).thenReturn(sourceRank);
        when(adminJobMapper.selectRankById("JG002")).thenReturn(rank("JG002", "Staff", 2));
        when(adminJobMapper.countEmployeesByRankAndIds("JG001", request.getEmpIds())).thenReturn(2);

        AdminJobServiceImpl service = service();

        RankResponseDTO response = service.revokeRank("JG001", request);

        assertThat(response).isEqualTo(sourceRank);
        verify(adminJobMapper).updateSelectedEmployeesRank("JG001", request.getEmpIds());
        verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(request.getEmpIds());
    }

    private AdminJobServiceImpl service() {
        return new AdminJobServiceImpl(authorizationService, adminJobMapper, rbacAuthorizationChangeService);
    }

    private RankCreateRequestDTO createRequest() {
        RankCreateRequestDTO request = new RankCreateRequestDTO();
        request.setRankId("JG001");
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
