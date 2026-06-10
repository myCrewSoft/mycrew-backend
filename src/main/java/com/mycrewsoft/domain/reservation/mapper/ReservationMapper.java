package com.mycrewsoft.domain.reservation.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.reservation.vo.ConfRmRsrvVO;
import com.mycrewsoft.domain.reservation.vo.ReservationDetailVO;

@Mapper
public interface ReservationMapper {

	// 회의실 예약 목록 조회
	List<ReservationDetailVO> selectConfRmRsrvList(
			@Param("beginDt") LocalDateTime beginDt,
            @Param("endDt") LocalDateTime endDt);
	
	// 회의실 예약 조회
	ReservationDetailVO selectConfRmRsrv(@Param("rsrvId") Long rsrvId);
	
	// 회의실 예약 생성
	int insertConfRmRsrv(ConfRmRsrvVO confRmRsrv);
	
	// 회의실 예약 정보 수정
	int updateConfRmRsrv(ConfRmRsrvVO confRmRsrv);
	
	// 회의실 예약 삭제
	int deleteConfRmRsrv(@Param("rsrvId") Long rsrvId);
	
	// 시간 중복 예약 존재 여부 확인
	int countOverlappingRsrv(
			@Param("confRmId") Long confRmId,
			@Param("beginDt") LocalDateTime beginDt,
			@Param("endDt") LocalDateTime endDt,
			@Param("rsrvId") Long rsrvId
	);
}
