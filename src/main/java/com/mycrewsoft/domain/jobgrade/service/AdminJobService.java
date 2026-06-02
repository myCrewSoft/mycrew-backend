package com.mycrewsoft.domain.jobgrade.service;

import java.util.List;

import com.mycrewsoft.domain.jobgrade.dto.request.RankAssignRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankCreateRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankDeleteRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankRevokeRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankUpdateRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.response.RankResponseDTO;

public interface AdminJobService {

    List<RankResponseDTO> getRanks();

    RankResponseDTO createRank(RankCreateRequestDTO request);

    RankResponseDTO updateRank(String rankId, RankUpdateRequestDTO request);

    void deleteRank(String rankId, RankDeleteRequestDTO request);

    RankResponseDTO assignRank(String rankId, RankAssignRequestDTO request);

    RankResponseDTO revokeRank(String rankId, RankRevokeRequestDTO request);
}
