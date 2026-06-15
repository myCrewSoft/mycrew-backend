package com.mycrewsoft.domain.room.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.room.dto.request.RoomCreateRequest;
import com.mycrewsoft.domain.room.dto.request.RoomUpdateRequest;
import com.mycrewsoft.domain.room.dto.response.RoomResponse;
import com.mycrewsoft.domain.room.mapper.RoomDtoMapper;
import com.mycrewsoft.domain.room.mapper.RoomMapper;
import com.mycrewsoft.domain.room.vo.ConfRmVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

	private final RoomMapper roomMapper;
	private final RoomDtoMapper roomDtoMapper;
	
	@Override
	public RoomResponse readRoom(Long confRmId) {
		
		ConfRmVO confRm = roomMapper.selectConfRm(confRmId);
		if(confRm == null) throw new CustomException(ErrorCode.CONF_RM_NOT_FOUND);
		
		// vo -> dto 변환
		RoomResponse room = roomDtoMapper.toResponse(confRm);
		
		return room;
	}

	@Override
	public List<RoomResponse> readRoomList() {
		
		List<ConfRmVO> confRmList = roomMapper.selectConfRmList();
		
		// vo -> dto 변환
		List<RoomResponse> roomList= roomDtoMapper.toResponseList(confRmList);
		
		return roomList;
	}

	@Override
	@Transactional
	public Long createRoom(RoomCreateRequest request) {
		
		// dto -> vo 변환
		ConfRmVO confRm = roomDtoMapper.toVO(request);
		
		// 회의실 생성
		int result = roomMapper.insertConfRm(confRm);
		if(result == 0) throw new CustomException(ErrorCode.CONF_RM_CREATE_FAILED);
		
		Long confRmId = confRm.getConfRmId();

		return confRmId;
	}

	@Override
	@Transactional
	public void modifyRoom(RoomUpdateRequest request) {
		// dto -> vo 변환
		ConfRmVO confRm = roomDtoMapper.toVO(request);
		
		// 회의실 수정
		int result = roomMapper.updateConfRm(confRm);
		if(result == 0) throw new CustomException(ErrorCode.CONF_RM_NOT_FOUND);
	}

	@Override
	@Transactional
	public void deleteRoom(Long confRmId) {
		// 권한 체크
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		if(currentEmpId == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
			
		ConfRmVO confRm = roomMapper.selectConfRm(confRmId);
		if (confRm == null) throw new CustomException(ErrorCode.CONF_RM_NOT_FOUND);
		if(!currentEmpId.equals(confRm.getConfRmMngrId())) throw new CustomException(ErrorCode.ACCESS_DENIED);

		// 삭제
		int result = roomMapper.deleteConfRm(confRmId); 
		if(result == 0) throw new CustomException(ErrorCode.CONF_RM_NOT_FOUND);
	}

}
