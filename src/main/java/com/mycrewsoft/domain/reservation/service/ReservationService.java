package com.mycrewsoft.domain.reservation.service;

import java.util.List;

import com.mycrewsoft.domain.reservation.dto.request.ReservationCreateRequest;
import com.mycrewsoft.domain.reservation.dto.request.ReservationUpdateRequest;
import com.mycrewsoft.domain.reservation.dto.response.ReservationResponse;

public interface ReservationService {

	ReservationResponse readReservation(Long rsrvId);
	
	List<ReservationResponse> readReservationList(String beginDt, String endDt);
	
	Long createReservation(ReservationCreateRequest request);
	
	void modifyReservation(Long rsrvId, ReservationUpdateRequest request);
	
	void deleteReservation(Long rervId);
}
