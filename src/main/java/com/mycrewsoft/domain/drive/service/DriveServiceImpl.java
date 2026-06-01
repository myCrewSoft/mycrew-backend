package com.mycrewsoft.domain.drive.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.mapper.DriveMapper;
import com.mycrewsoft.domain.drive.vo.DriveVo;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileService;
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
	private final FileService fileService;
	
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

	@Override
	@Transactional
	public DriveResponseDto uploadFile(FileUploadRequestDto fileReqDto, Long prntDriveItemId) {
		//공통 파일 업로드 (디스크 저장 + 공통통첨부파일 테이블 insert)
		 Long driveAtchFileId = fileService.upload(fileReqDto, "02");
		
		//드라이브 아이템 insert
		DriveVo vo = new DriveVo();
		vo.setPrntDriveItemId(prntDriveItemId);
		vo.setEmpId(SecurityUtil.getCurrentEmpId());
		vo.setItemNm(fileReqDto.getFile().getOriginalFilename());
		vo.setItemTypeCd("02");		//파일
		vo.setDriveAtchFileId(driveAtchFileId);
		
		int result = mapper.insertDriveItem(vo);
		
		if(result > 0) {
			DriveResponseDto respDto = dtoMapper.toDto(vo, DriveResponseDto.class);
			respDto.setFrstRegDt(DateUtil.format(vo.getFrstRegDt()));
			return respDto;
		}else {
			throw new CustomException(ErrorCode.DRIVE_INSERT_FAILED);
		}
	}

	/**
	 * 개인 드라이브 목록 조회
	 */
	@Override
	public List<DriveResponseDto> getMyDriveList() {
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// 드라이브 목록 조회
		List<DriveVo> voList = mapper.selectListByEmpId(empId);
		
		// vo -> dto 변환
		List<DriveResponseDto> respDtoList = voList.stream().map(vo -> {
	        DriveResponseDto dto = dtoMapper.toDto(vo, DriveResponseDto.class);
	        dto.setFrstRegDt(DateUtil.format(vo.getFrstRegDt()));
	        dto.setLastMdfcnDt(DateUtil.format(vo.getLastMdfcnDt()));
	        dto.setTimeAgo(DateUtil.timeAgo(vo.getFrstRegDt()));
	        return dto;
	    }).collect(Collectors.toList());
		return respDtoList;
	}

}
