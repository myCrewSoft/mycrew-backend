package com.mycrewsoft.domain.schedule.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.schedule.vo.SchdTargetVO;

@Mapper
public interface SchdTargetMapper {

	int insertSchdTargetList(@Param("list") List<SchdTargetVO> voList);
	
	int deleteSchdTarget(Long schdId);
	
}
