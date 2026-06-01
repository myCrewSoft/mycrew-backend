package com.mycrewsoft.domain.schedule.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdSearchVO;

@Mapper
public interface IntgSchdMapper {

	// 일정 단건 조회
	IntgSchdVO selectIntgSchd(@Param("schdId") Long schdId);

	// 리스트 조회
	List<IntgSchdVO> selectIntgSchdList(SchdSearchVO searchVo);
	
	/**
	 * 일정 생성
	 * <selectKey>로 schdId를 vo에 세팅하는 게 목적이라 건수 확인 불필요
	 * @param intgSchd
	 */
	void insertIntgSchd(IntgSchdVO intgSchd);
	
	// 일정 수정
	int updateIntgSchd(IntgSchdVO intgSchd);
	
	// 일정 삭제(논리)
	int deleteIntgSchd(@Param("schdId") Long schdId);
}
