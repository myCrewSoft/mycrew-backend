package com.mycrewsoft.domain.mtng.service;

import java.util.List;

import com.mycrewsoft.domain.mtng.dto.request.MtngCreateRequest;
import com.mycrewsoft.domain.mtng.dto.request.MtngListRequest;
import com.mycrewsoft.domain.mtng.dto.request.MtngUpdateRequest;
import com.mycrewsoft.domain.mtng.dto.response.MtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.MtngListResponse;

public interface MtngService {

    Long createMtng(MtngCreateRequest request);

    List<MtngListResponse> getMtngList(MtngListRequest request);

    MtngDetailResponse getMtngDetail(Long mtngId);

    void updateMtng(Long mtngId, MtngUpdateRequest request);

    void deleteMtng(Long mtngId);
}