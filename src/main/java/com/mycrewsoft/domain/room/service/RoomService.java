package com.mycrewsoft.domain.room.service;

import java.util.List;

import com.mycrewsoft.domain.room.dto.request.RoomCreateRequest;
import com.mycrewsoft.domain.room.dto.request.RoomUpdateRequest;
import com.mycrewsoft.domain.room.dto.response.RoomResponse;

public interface RoomService {

	RoomResponse readRoom(Long confRmId);
	
	List<RoomResponse> readRoomList();
	
	Long createRoom(RoomCreateRequest request);
	
	void modifyRoom(RoomUpdateRequest request);
	
	void deleteRoom(Long confRmId);
}
