package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mtng.dto.request.AdminMtngListRequest;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngAnalyticsItemResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngAnalyticsResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngListPageResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngListResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngPtcptResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngStatsResponse;
import com.mycrewsoft.domain.mtng.mapper.AdminMtngDtoMapper;
import com.mycrewsoft.domain.mtng.mapper.AdminMtngMapper;
import com.mycrewsoft.domain.mtng.vo.AdminMtngStatsVO;
import com.mycrewsoft.domain.mtng.vo.MtngListVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMtngServiceImpl implements AdminMtngService {

    private final AdminMtngMapper adminMtngMapper;
    private final AdminMtngDtoMapper adminMtngDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminMtngListPageResponse getAdminMtngList(AdminMtngListRequest request) {
        List<MtngListVO> voList = adminMtngMapper.selectAdminMtngList(request);
        int totalCount = adminMtngMapper.selectAdminMtngTotalCount(request);

        List<AdminMtngListResponse> meetings = adminMtngDtoMapper.toListResponseList(voList);
        int totalPages = (int) Math.ceil((double) totalCount / request.getSize());

        return AdminMtngListPageResponse.builder()
                .meetings(meetings)
                .totalCount(totalCount)
                .currentPage(request.getPage())
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminMtngDetailResponse getAdminMtngDetail(Long mtngId) {
        MtngListVO vo = adminMtngMapper.selectAdminMtngDetail(mtngId);

        if (vo == null) {
            throw new CustomException(ErrorCode.MTNG_NOT_FOUND);
        }

        List<AdminMtngPtcptResponse> ptcpts = adminMtngDtoMapper.toPtcptResponseList(
                adminMtngMapper.selectAdminMtngPtcptList(mtngId)
        );

        return adminMtngDtoMapper.toDetailResponse(vo)
                .toBuilder()
                .ptcpts(ptcpts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminMtngStatsResponse getAdminMtngStats() {
        AdminMtngStatsVO vo = adminMtngMapper.selectAdminMtngStats();
        return adminMtngDtoMapper.toStatsResponse(vo);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminMtngAnalyticsResponse getAdminMtngAnalytics() {
        List<AdminMtngAnalyticsItemResponse> typeStats =
                adminMtngDtoMapper.toAnalyticsItemResponseList(adminMtngMapper.selectMtngTypeStats());
        List<AdminMtngAnalyticsItemResponse> monthlyStats =
                adminMtngDtoMapper.toAnalyticsItemResponseList(adminMtngMapper.selectMtngMonthlyStats());
        List<AdminMtngAnalyticsItemResponse> hourlyStats =
                adminMtngDtoMapper.toAnalyticsItemResponseList(adminMtngMapper.selectMtngHourlyStats());
        int momGenerationRate = adminMtngMapper.selectMomGenerationRate();

        return AdminMtngAnalyticsResponse.builder()
                .typeStats(typeStats)
                .monthlyStats(monthlyStats)
                .hourlyStats(hourlyStats)
                .momGenerationRate(momGenerationRate)
                .build();
    }

    @Override
    @Transactional
    public void forceEndMtng(Long mtngId) {
        int updated = adminMtngMapper.updateMtngForceEnd(mtngId);

        if (updated == 0) {
            throw new CustomException(ErrorCode.MTNG_NOT_IN_PROGRESS);
        }
    }
}