package com.mycrewsoft.domain.jobposition.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.jobgrade.controller.AdminJobController;
import com.mycrewsoft.domain.jobgrade.dto.request.RankAssignRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankCreateRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankDeleteRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankRevokeRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankUpdateRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.response.RankResponseDTO;
import com.mycrewsoft.domain.jobgrade.service.AdminJobService;

class AdminJobControllerTest {

    @Test
    void getRanksReturnsServiceResponse() {
        AdminJobService service = Mockito.mock(AdminJobService.class);
        AdminJobController controller = new AdminJobController(service);
        RankResponseDTO rank = new RankResponseDTO();
        rank.setRankId("JOB01");
        when(service.getRanks()).thenReturn(List.of(rank));

        ApiResponse<List<RankResponseDTO>> response = controller.getRanks();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsExactly(rank);
        verify(service).getRanks();
    }

    @Test
    void createRankDelegatesRequestToService() {
        AdminJobService service = Mockito.mock(AdminJobService.class);
        AdminJobController controller = new AdminJobController(service);
        RankCreateRequestDTO request = new RankCreateRequestDTO();
        RankResponseDTO rank = new RankResponseDTO();
        rank.setRankId("JOB01");
        when(service.createRank(request)).thenReturn(rank);

        ApiResponse<RankResponseDTO> response = controller.createRank(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(rank);
        verify(service).createRank(request);
    }

    @Test
    void updateRankDelegatesRequestToService() {
        AdminJobService service = Mockito.mock(AdminJobService.class);
        AdminJobController controller = new AdminJobController(service);
        RankUpdateRequestDTO request = new RankUpdateRequestDTO();
        RankResponseDTO rank = new RankResponseDTO();
        rank.setRankId("JOB01");
        when(service.updateRank("JOB01", request)).thenReturn(rank);

        ApiResponse<RankResponseDTO> response = controller.updateRank("JOB01", request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(rank);
        verify(service).updateRank("JOB01", request);
    }

    @Test
    void deleteRankDelegatesReplacementRequestToService() {
        AdminJobService service = Mockito.mock(AdminJobService.class);
        AdminJobController controller = new AdminJobController(service);
        RankDeleteRequestDTO request = new RankDeleteRequestDTO();
        request.setReplacementRankId("JOB02");

        ApiResponse<String> response = controller.deleteRank("JOB01", request);

        assertThat(response.isSuccess()).isTrue();
        verify(service).deleteRank("JOB01", request);
    }

    @Test
    void assignRankDelegatesRequestToService() {
        AdminJobService service = Mockito.mock(AdminJobService.class);
        AdminJobController controller = new AdminJobController(service);
        RankAssignRequestDTO request = new RankAssignRequestDTO();
        RankResponseDTO rank = new RankResponseDTO();
        rank.setRankId("JOB01");
        when(service.assignRank("JOB01", request)).thenReturn(rank);

        ApiResponse<RankResponseDTO> response = controller.assignRank("JOB01", request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(rank);
        verify(service).assignRank("JOB01", request);
    }

    @Test
    void revokeRankDelegatesRequestToService() {
        AdminJobService service = Mockito.mock(AdminJobService.class);
        AdminJobController controller = new AdminJobController(service);
        RankRevokeRequestDTO request = new RankRevokeRequestDTO();
        RankResponseDTO rank = new RankResponseDTO();
        rank.setRankId("JOB01");
        when(service.revokeRank("JOB01", request)).thenReturn(rank);

        ApiResponse<RankResponseDTO> response = controller.revokeRank("JOB01", request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(rank);
        verify(service).revokeRank("JOB01", request);
    }
}
