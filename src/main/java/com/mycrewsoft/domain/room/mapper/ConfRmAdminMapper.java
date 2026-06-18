package com.mycrewsoft.domain.room.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.room.vo.ConfRmAdminVO;
import com.mycrewsoft.domain.room.vo.ConfRmSummaryVO;

@Mapper
public interface ConfRmAdminMapper {

	ConfRmSummaryVO selectConfRmStatsSummary();

	List<ConfRmAdminVO> selectConfRmList();
}