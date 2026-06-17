package com.mycrewsoft.domain.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.reservation.dto.request.RsrvSearchRequest;
import com.mycrewsoft.domain.reservation.dto.response.PopularRmItem;
import com.mycrewsoft.domain.reservation.dto.response.RsrvListItem;
import com.mycrewsoft.domain.reservation.dto.response.RsrvStatsSummary;
import com.mycrewsoft.domain.reservation.mapper.ReservationDtoMapper;
import com.mycrewsoft.domain.reservation.mapper.ReservationMapper;
import com.mycrewsoft.domain.reservation.mapper.RsrvAdminMapper;
import com.mycrewsoft.domain.reservation.vo.PopularRmVO;
import com.mycrewsoft.domain.reservation.vo.ReservationDetailVO;
import com.mycrewsoft.domain.reservation.vo.RsrvAdminVO;
import com.mycrewsoft.domain.reservation.vo.RsrvSearchVO;
import com.mycrewsoft.domain.reservation.vo.RsrvSummaryVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RsrvAdminServiceImpl implements RsrvAdminService {

    private final RsrvAdminMapper rsrvAdminMapper;
    private final ReservationMapper reservationMapper;
    private final ReservationDtoMapper reservationDtoMapper;

    @Override
    public RsrvStatsSummary readRsrvStatsSummary() {
        RsrvSummaryVO summaryVO = rsrvAdminMapper.selectRsrvStatsSummary();
        List<PopularRmVO> popularRmVOList = rsrvAdminMapper.selectPopularRmList();

        List<PopularRmItem> popularRmList = popularRmVOList.stream()
                .map(reservationDtoMapper::toPopularRmItem)
                .toList();

        return RsrvStatsSummary.builder()
                .todayRsrvCount(summaryVO.getTodayRsrvCount())
                .weekRsrvCount(summaryVO.getWeekRsrvCount())
                .availableRmCount(summaryVO.getAvailableRmCount())
                .popularRmList(popularRmList)
                .build();
    }
    
    @Override
    public List<RsrvListItem> readRsrvList(RsrvSearchRequest request) {
        RsrvSearchVO rsrvSearchVO = reservationDtoMapper.toRsrvSearchVO(request);
        List<RsrvAdminVO> voList = rsrvAdminMapper.selectRsrvList(rsrvSearchVO);
        return voList.stream()
                .map(reservationDtoMapper::toRsrvListItem)
                .toList();
    }

    @Override
    @Transactional
    public void cancelRsrv(Long rsrvId) {
        ReservationDetailVO rsrv = reservationMapper.selectConfRmRsrv(rsrvId);
        if (rsrv == null) {
            throw new CustomException(ErrorCode.RSRV_NOT_FOUND);
        }
        if ("Y".equals(rsrv.getDelYn())) {
            throw new CustomException(ErrorCode.RSRV_ALREADY_CANCELLED);
        }
        reservationMapper.deleteConfRmRsrv(rsrvId);
    }
}