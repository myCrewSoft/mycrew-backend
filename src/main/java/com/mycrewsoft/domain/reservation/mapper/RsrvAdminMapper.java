package com.mycrewsoft.domain.reservation.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.reservation.vo.PopularRmVO;
import com.mycrewsoft.domain.reservation.vo.RsrvAdminVO;
import com.mycrewsoft.domain.reservation.vo.RsrvSearchVO;
import com.mycrewsoft.domain.reservation.vo.RsrvSummaryVO;

@Mapper
public interface RsrvAdminMapper {

    RsrvSummaryVO selectRsrvStatsSummary();

    List<PopularRmVO> selectPopularRmList();

    List<RsrvAdminVO> selectRsrvList(RsrvSearchVO rsrvSearchVO);
}