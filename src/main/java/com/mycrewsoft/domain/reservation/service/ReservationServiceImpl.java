package com.mycrewsoft.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.domain.reservation.dto.request.ReservationCreateRequest;
import com.mycrewsoft.domain.reservation.dto.request.ReservationUpdateRequest;
import com.mycrewsoft.domain.reservation.dto.response.ReservationResponse;
import com.mycrewsoft.domain.reservation.mapper.ReservationDtoMapper;
import com.mycrewsoft.domain.reservation.mapper.ReservationMapper;
import com.mycrewsoft.domain.reservation.vo.ConfRmRsrvVO;
import com.mycrewsoft.domain.reservation.vo.ReservationDetailVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

	private final ReservationMapper reservationMapper;
	private final ReservationDtoMapper reservationDtoMapper;
	
	private static final int WIDGET_RESERVATION_LIMIT = 2;
	
	// 예약 상세 조회
	@Override
	public ReservationResponse readReservation(Long rsrvId) {
		
		// 예약 조회
		ReservationDetailVO confRmRsrv = reservationMapper.selectConfRmRsrv(rsrvId);
		if(confRmRsrv == null) throw new CustomException(ErrorCode.RSRV_NOT_FOUND);
		
		// vo -> dto 변환
		ReservationResponse response = reservationDtoMapper.toResponse(confRmRsrv);
	    response.setMine(confRmRsrv.getRsrvEmpId().equals(SecurityUtil.getCurrentEmpId()));
		return response;
	}

	// 예약 목록 조회(사용 중인 회의실)
	@Override
	public List<ReservationResponse> readReservationList(String begin, String end) {
		
		// 날짜 타입 변환
		LocalDateTime beginDt = DateUtil.parseDate(begin).atStartOfDay();
		LocalDateTime endDt = DateUtil.parseDate(end).atTime(23, 59, 59);

		// 사용자 ID 조회
	    Long empId = SecurityUtil.getCurrentEmpId();

		// 예약 조회
		List<ReservationDetailVO> confRmRsrvList = reservationMapper.selectConfRmRsrvList(beginDt, endDt);
	
		// vo -> dto 변환
		return confRmRsrvList.stream()
		        .map(vo -> {
		            ReservationResponse response = reservationDtoMapper.toResponse(vo);
		            response.setMine(vo.getRsrvEmpId().equals(empId));
		            return response;
		        })
		        .toList();
	}

	// 예약 생성
	@Override
 	@Transactional
	public Long createReservation(ReservationCreateRequest request) {
		
		// dto -> vo 변환
		ConfRmRsrvVO confRmRsrv = reservationDtoMapper.toVo(request);
		confRmRsrv.setRsrvEmpId(SecurityUtil.getCurrentEmpId());

	    // 종일 예약이면 시작/종료 시간 강제 설정
	    if ("Y".equals(request.getAllDayYn())) {
	        confRmRsrv.setBeginDt(request.getStartDateTime().toLocalDate().atStartOfDay());
	        confRmRsrv.setEndDt(request.getEndDateTime().toLocalDate().atTime(23, 59));
	    }
	    
        // 중복 예약 체크
        int overlap = reservationMapper.countOverlappingRsrv(
                confRmRsrv.getConfRmId(),
                confRmRsrv.getBeginDt(),
                confRmRsrv.getEndDt(),
                0L
        );
        if (overlap > 0) throw new CustomException(ErrorCode.RSRV_TIME_CONFLICT);
        
		// 예약 생성
		int result = reservationMapper.insertConfRmRsrv(confRmRsrv);
		if(result == 0) throw new CustomException(ErrorCode.RSRV_CREATE_FAILED);

		// 예약 ID 반환
		return confRmRsrv.getRsrvId();
	}

	@Override
 @Transactional
	public void modifyReservation(Long rsrvId, ReservationUpdateRequest request) {

		// 검증(본인이 예약 담당자인지)
		Long empId = SecurityUtil.getCurrentEmpId();
		ReservationDetailVO reservation = reservationMapper.selectConfRmRsrv(rsrvId);
		if(reservation == null) throw new CustomException(ErrorCode.RSRV_NOT_FOUND);
		Long rsrvEmpId = reservation.getRsrvEmpId();
		if(!rsrvEmpId.equals(empId)) throw new CustomException(ErrorCode.ACCESS_DENIED);
		
		// dto -> vo 변환
		ConfRmRsrvVO confRmRsrv = reservationDtoMapper.toVo(request);
		confRmRsrv.setRsrvId(rsrvId);
		
	    // 종일 예약이면 시작/종료 시간 강제 설정
	    if ("Y".equals(request.getAllDayYn())) {
	        confRmRsrv.setBeginDt(request.getStartDateTime().toLocalDate().atStartOfDay());
	        confRmRsrv.setEndDt(request.getEndDateTime().toLocalDate().atTime(23, 59));
	    }

	    // 중복 예약 체크 (본인 예약은 제외)
	    int overlap = reservationMapper.countOverlappingRsrv(
	            confRmRsrv.getConfRmId(),
	            confRmRsrv.getBeginDt(),
	            confRmRsrv.getEndDt(),
	            confRmRsrv.getRsrvId()
	    );
	    if (overlap > 0) throw new CustomException(ErrorCode.RSRV_TIME_CONFLICT);
	    
		int result = reservationMapper.updateConfRmRsrv(confRmRsrv);
		if(result == 0) throw new CustomException(ErrorCode.RSRV_NOT_FOUND);		
	}

	@Override
	@Transactional
	public void deleteReservation(Long rsrvId) {

		// 검증(본인이 예약 담당자인지)
		Long empId = SecurityUtil.getCurrentEmpId();
		ReservationDetailVO confRmRsrv = reservationMapper.selectConfRmRsrv(rsrvId);
		if(confRmRsrv == null) throw new CustomException(ErrorCode.RSRV_NOT_FOUND);
		if(!confRmRsrv.getRsrvEmpId().equals(empId)) throw new CustomException(ErrorCode.ACCESS_DENIED);
		
		int result = reservationMapper.deleteConfRmRsrv(rsrvId);
		if(result == 0) throw new CustomException(ErrorCode.RSRV_NOT_FOUND);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ReservationResponse> readReservationListForWidget() {
	    Long empId = SecurityUtil.getCurrentEmpId();
	    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
	    LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

	    List<ReservationDetailVO> voList = reservationMapper.selectMyReservationListForWidget(
    		empId,
    		DateUtil.startOfToday(),
            DateUtil.endOfToday(),
            WIDGET_RESERVATION_LIMIT
	    );

	    return voList.stream()
	            .map(vo -> {
	                ReservationResponse response = reservationDtoMapper.toResponse(vo);
	                response.setMine(true);
	                return response;
	            })
	            .toList();
	}

}
