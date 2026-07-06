package com.mycrewsoft.domain.room.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.room.vo.ConfRmVO;

@Mapper
public interface RoomMapper {

	// 회의실 목록 조회
	List<ConfRmVO> selectConfRmList();
	
	// 회의실 상세 조회
	ConfRmVO selectConfRm(@Param("confRmId") Long confRmId);

	// 회의실 생성
	int insertConfRm(ConfRmVO confRm);
	
	// 회의실 정보 수정
	int updateConfRm(ConfRmVO confRm);
	
	// 회의실 삭제
	int deleteConfRm(@Param("confRmId") Long confRmId);
}
