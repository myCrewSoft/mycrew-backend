package com.mycrewsoft.domain.drive.service;

import org.springframework.stereotype.Service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.mapper.DriveMapper;
import com.mycrewsoft.domain.drive.vo.DriveVo;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

/**
 * 드라이브 비지니스 로직 구현체
 */
@Service
@RequiredArgsConstructor
public class DriveServiceImpl implements DriveService {
	private final DtoMapper dtoMapper;
	private final DriveMapper mapper;
	
	/**
	 * 폴더 생성
	 */
	@Override
	public DriveResponseDto createFolder(DriveFolderCreateRequestDto reqDto) {
		
		Long empId = SecurityUtil.getCurrentEmpId();
		
		DriveVo vo = new DriveVo();
		vo.setPrntDriveItemId(reqDto.getPrntDriveItemId());
		vo.setItemNm(reqDto.getItemNm());
		vo.setItemTypeCd("01"); 	//아이템 유형 01: 폴더
		vo.setEmpId(empId);
		
		int result = mapper.insertDriveItem(vo);
		
		if(result > 0) {
			DriveResponseDto respDto = dtoMapper.toDto(vo, DriveResponseDto.class);
			respDto.setFrstRegDt(DateUtil.format(vo.getFrstRegDt()));
			return respDto;
		}else {
			throw new CustomException(ErrorCode.DRIVE_INSERT_FAILED);
		}
		
		
	}

}
