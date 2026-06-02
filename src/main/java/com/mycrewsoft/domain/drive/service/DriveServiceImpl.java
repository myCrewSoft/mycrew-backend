package com.mycrewsoft.domain.drive.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.common.util.FileUtil;
import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.mapper.DriveMapper;
import com.mycrewsoft.domain.drive.vo.DriveVo;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileService;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.PermissionCode;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
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
	private final AuthorizationService authorizationService;
	
	// 공통 : 현재 사용자 소유 아이템 권한 컨텍스트 빌드
	private ResourceContext buildOwnerContext() {
	    return ResourceContext.builder()
	            .resourceType(ResourceType.DRIVE)
	            .ownerEmpId(SecurityUtil.getCurrentEmpId())
	            .build();
	}
	
	/**
	 * 폴더 생성
	 */
	@Override
	public DriveResponseDto createFolder(DriveFolderCreateRequestDto reqDto) {
		// 권한 체크 추가
	    authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_UPLOAD, buildOwnerContext());
		
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

	/**
	 * 드라이브 파일 업로드
	 */
	@Override
	@Transactional
	public DriveResponseDto uploadFile(FileUploadRequestDto fileReqDto, Long prntDriveItemId) {
		 // 권한 체크 추가
	    authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_UPLOAD, buildOwnerContext());
		
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
	public List<DriveResponseDto> getMyDriveList(Long prntDriveItemId) {
		// 권한 체크 추가
	    authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_READ, buildOwnerContext());
		
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// 드라이브 목록 조회
		List<DriveVo> voList = mapper.selectListByEmpId(empId, prntDriveItemId);
		
		// vo -> dto 변환
		List<DriveResponseDto> respDtoList = voList.stream().map(vo -> {
	        DriveResponseDto dto = dtoMapper.toDto(vo, DriveResponseDto.class);
	        dto.setFrstRegDt(DateUtil.format(vo.getFrstRegDt()));
	        dto.setLastMdfcnDt(DateUtil.format(vo.getLastMdfcnDt()));
	        dto.setTimeAgo(DateUtil.timeAgo(vo.getFrstRegDt()));
	        dto.setFileSz(vo.getFileSz() != null ? FileUtil.formatFileSize(vo.getFileSz()) : null);
	        return dto;
	    }).collect(Collectors.toList());
		return respDtoList;
	}

	/**
	 * 드라이브 폴더명 수정
	 */
	@Override
	@Transactional
	public DriveResponseDto renameItem(Long driveItemId, String itemNm) {
		//아이템 존재 확인
		DriveVo item = mapper.selectDriveItemById(driveItemId);
		if(item == null) {
			throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		}
		
		//폴더명만 수정 가능
		if(!"01".equals(item.getItemTypeCd())) {
			throw new CustomException(ErrorCode.DRIVE_RENAME_NOT_ALLOWED);
		}
		
		//권한 체크
		authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_UPDATE,
	            ResourceContext.builder()
	                    .resourceType(ResourceType.DRIVE)
	                    .ownerEmpId(item.getEmpId())
	                    .build());
		
		//검증 통과 후 폴더명 수정
	    int result = mapper.updateFolderName(driveItemId, itemNm);
	    if (result == 0) {
	        throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
	    }
	    
	    // 폴더명 수정 후 재조회 대신 바뀐 이름으로 세팅
	    DriveResponseDto respDto = dtoMapper.toDto(item, DriveResponseDto.class);
	    respDto.setItemNm(itemNm);
	    respDto.setLastMdfcnDt(DateUtil.format(LocalDateTime.now()));
		
		return respDto;
	}

	/**
	 * 즐겨찾기 등록
	 */
	@Override
	@Transactional
	public DriveResponseDto registerBookmark(Long driveItemId) {
		//해당 아이템 존재 확인
		DriveVo item = mapper.selectDriveItemById(driveItemId);
		if(item == null) {
			throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		}
		
		//권한체크
		authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_UPDATE,
	            ResourceContext.builder()
	                    .resourceType(ResourceType.DRIVE)
	                    .ownerEmpId(item.getEmpId())
	                    .build());
		
		//즐겨찾기 등록
		mapper.updateBookmarkYn(driveItemId);
		
		DriveResponseDto respDto = dtoMapper.toDto(item, DriveResponseDto.class);
		respDto.setBookmarkYn("Y");
		respDto.setLastMdfcnDt(DateUtil.format(LocalDateTime.now()));
		
		return respDto;
	}

	@Override
	public DriveResponseDto deleteBookmark(Long driveItemId) {
		//해당 아이템 존재 확인
		DriveVo item = mapper.selectDriveItemById(driveItemId);
		if(item == null) {
			throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		}
		
		//권한 체크
		authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_UPDATE,
	            ResourceContext.builder()
	                    .resourceType(ResourceType.DRIVE)
	                    .ownerEmpId(item.getEmpId())
	                    .build());
		//즐겨찾기 해제
		mapper.deleteBookmark(driveItemId);
		
		//DB다시 조회하지 않고 여부 즐겨찾기여부 "N"으로 세팅
		DriveResponseDto respDto = dtoMapper.toDto(item, DriveResponseDto.class);
		respDto.setBookmarkYn("N");
		respDto.setLastMdfcnDt(DateUtil.format(LocalDateTime.now()));
		
		return respDto;
	}

	/**
	 * 드라이브 아이템 단건 삭제 (하위 포함)
	 */
	@Override
	@Transactional
	public void deleteItem(Long driveItemId) {
		//아이템 존재 확인
		DriveVo item = mapper.selectDriveItemById(driveItemId);
		if(item == null) {
			throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		}
		
		//삭제 권한 체크
		authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_DELETE,
	            ResourceContext.builder()
	                    .resourceType(ResourceType.DRIVE)
	                    .ownerEmpId(item.getEmpId())
	                    .build());

	    Long deltrMbrId = SecurityUtil.getCurrentEmpId();
	    
	    if ("02".equals(item.getItemTypeCd())) {
	        // 파일 — 첨부파일 소프트 딜리트
	        if (item.getDriveAtchFileId() != null) {
	            fileService.deleteFile(item.getDriveAtchFileId());
	        }
	    } else {
	        // 폴더 — 하위 파일들만 첨부파일 소프트 딜리트
	        List<DriveVo> fileChildren = mapper.selectFileChildrenByItemId(driveItemId);
	        for (DriveVo child : fileChildren) {
	            if ("02".equals(child.getItemTypeCd()) && child.getDriveAtchFileId() != null) {
	                fileService.deleteFile(child.getDriveAtchFileId()); 
	            }
	        }
	    }

	    // TB_DRIVE 소프트 딜리트 (하위 포함)
	    mapper.softDeleteItemWithChildren(driveItemId, deltrMbrId);
	}



}
