package com.mycrewsoft.domain.drive.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.common.util.FileUtil;
import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.dto.DriveSearchRequestDto;
import com.mycrewsoft.domain.drive.mapper.DriveMapper;
import com.mycrewsoft.domain.drive.vo.DriveVo;
import com.mycrewsoft.domain.file.constant.FileConstants;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileService;
import com.mycrewsoft.security.authz.AuthorizationService;
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
		vo.setFrstRgtrId(empId);
		vo.setDriveScopeCd("01");	//드라이브 분류 01:개인드라이브
		
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
		 Long driveAtchFileId = fileService.upload(fileReqDto, FileConstants.DRIVE);
		
		//드라이브 아이템 insert
		DriveVo vo = new DriveVo();
		vo.setPrntDriveItemId(prntDriveItemId);
		vo.setFrstRgtrId(SecurityUtil.getCurrentEmpId());
		vo.setItemNm(fileReqDto.getFile().getOriginalFilename());
		vo.setItemTypeCd("02");		//파일
		vo.setDriveAtchFileId(driveAtchFileId);
		vo.setDriveScopeCd("01");
		
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
	public Page<DriveResponseDto> getMyDriveList(DriveSearchRequestDto reqDto) {
		// 권한 체크
	    authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_READ, buildOwnerContext());
		
		Long empId = SecurityUtil.getCurrentEmpId();
		int offset = reqDto.getPage() * reqDto.getSize();
		
		//전체 카운트
		long total = mapper.countDriveList(empId, reqDto.getPrntDriveItemId());
		
		//페이징된 데이터 조회
		List<DriveVo> voList = mapper.selectListByEmpId(empId, reqDto.getPrntDriveItemId(), offset, reqDto.getSize());
		
		// vo -> dto 변환
		List<DriveResponseDto> content = voList.stream().map(vo -> {
	        DriveResponseDto dto = dtoMapper.toDto(vo, DriveResponseDto.class);
	        dto.setFrstRegDt(DateUtil.format(vo.getFrstRegDt()));
	        dto.setLastMdfcnDt(DateUtil.format(vo.getLastMdfcnDt()));
	        dto.setFileSz(vo.getFileSz() != null ? FileUtil.formatFileSize(vo.getFileSz()) : null);
	        return dto;
	    }).collect(Collectors.toList());
	
		PageRequest pageable = PageRequest.of(reqDto.getPage(), reqDto.getSize());
		return new PageImpl<>(content, pageable, total);
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
	                    .ownerEmpId(item.getFrstRgtrId())
	                    .build());
		
		//검증 통과 후 폴더명 수정
	    int result = mapper.updateFolderName(driveItemId, itemNm, SecurityUtil.getCurrentEmpId());
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
	 * 즐겨찾기 등록/해제
	 */
	@Override
	@Transactional
	public DriveResponseDto toggleBookmark(Long driveItemId) {
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
	                    .ownerEmpId(item.getFrstRgtrId())
	                    .build());
		
		//즐겨찾기 등록
		String newBookmarkYn = "Y".equals(item.getBookmarkYn()) ? "N" : "Y";
		mapper.updateBookmarkYn(driveItemId, newBookmarkYn, SecurityUtil.getCurrentEmpId());
		
		//재조회 안 하고 값 세팅 후 반환
		DriveResponseDto respDto = dtoMapper.toDto(item, DriveResponseDto.class);
		respDto.setBookmarkYn(newBookmarkYn);
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
	                    .ownerEmpId(item.getFrstRgtrId())
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

	/**
	 * 개인 드라이브 휴지통 목록 조회
	 */
	@Override
	public List<DriveResponseDto> getTrashList() {
		//권한체크
		authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_READ, buildOwnerContext());

	    Long empId = SecurityUtil.getCurrentEmpId();
	    List<DriveVo> voList = mapper.selectTrashList(empId);

	    return voList.stream().map(vo -> {
	        DriveResponseDto respdto = dtoMapper.toDto(vo, DriveResponseDto.class);
	        respdto.setDelDt(DateUtil.format(vo.getDelDt()));
	        respdto.setFrstRegDt(DateUtil.format(vo.getFrstRegDt()));
	        respdto.setFileSz(vo.getFileSz() != null ? FileUtil.formatFileSize(vo.getFileSz()) : null);
	        return respdto;
	    }).collect(Collectors.toList());
	}

	/**
	 * 휴지통 단건 복원 (삭제 상태 변경)
	 */
	@Override
	@Transactional
	public void restoreItem(Long driveItemId) {
		//아이템 존재 확인 및 권한 체크
		DriveVo item = mapper.selectTrashItemById(driveItemId);
		if(item == null) {
			throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		}
		
		//권한 체크
		authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_UPDATE,
	            ResourceContext.builder()
	                    .resourceType(ResourceType.DRIVE)
	                    .ownerEmpId(item.getFrstRgtrId())
	                    .build());
		
		//1. 파일이면 첨부파일 복원
		if("02".equals(item.getItemTypeCd()) && item.getDriveAtchFileId() != null) {
			fileService.restoreFile(item.getDriveAtchFileId());
		}
		
		//2. 폴더면 하위 전체를 조회한 후 파일인 것만 복원
		// (폴더만 있다면 조회에서 파일 없으니까 for문 그냥 통과)
		if ("01".equals(item.getItemTypeCd())) {
		    List<DriveVo> fileChildren = mapper.selectTrashFileChildrenByItemId(driveItemId);
		    for (DriveVo child : fileChildren) {
		        if ("02".equals(child.getItemTypeCd()) && child.getDriveAtchFileId() != null) {
		            fileService.restoreFile(child.getDriveAtchFileId());
		        }
		    }
		}
		
		// TB_DRIVE 복원 (하위 포함)
		mapper.restoreItem(driveItemId);
	}

	/**
	 * 영구 삭제
	 */
	@Override
	@Transactional
	public void hardDeleteItem(Long driveItemId) {
		DriveVo item = mapper.selectTrashItemById(driveItemId);
		if(item == null) {
			throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		}
		
		authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_DELETE,
	            ResourceContext.builder()
	                    .resourceType(ResourceType.DRIVE)
	                    .ownerEmpId(item.getFrstRgtrId())
	                    .build());
		
		//1. 파일이면 첨부파일 영구 삭제 
		if("02".equals(item.getItemTypeCd()) && item.getDriveAtchFileId() != null) {
			fileService.hardDeleteFile(item.getDriveAtchFileId());
		}
		
		// 폴더면 하위 전체 조회 후 파일인 것만 첨부파일 영구 삭제
		if("01".equals(item.getItemTypeCd())) {
			List<DriveVo> fileChildren = mapper.selectTrashFileChildrenByItemId(driveItemId);
			for(DriveVo child : fileChildren) {
				if("02".equals(child.getItemTypeCd()) && child.getDriveAtchFileId() != null) {
					fileService.hardDeleteFile(child.getDriveAtchFileId());
				}
			}
		}
		
		//TB_DRIVE 물리 삭제 (하위 포함)
		int result = mapper.hardDeleteItem(driveItemId);
		if(result == 0) {
			throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		}
		
	}

	/**
	 * 파일 다운로드
	 */
	@Override
	public ResponseEntity<Resource> downloadFile(Long driveItemId) {
		//아이템 존재 확인
		DriveVo item = mapper.selectDriveItemById(driveItemId);
		if(item == null) {
			throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		}
		
		//파일 아이템인지 확인
		if(!"02".equals(item.getItemTypeCd())) {
			throw new CustomException(ErrorCode.DRIVE_NOT_A_FILE);
		}
		
		//권한 체크
		authorizationService.assertCurrentUserPermission(
	            PermissionCode.DRIVE_READ,
	            ResourceContext.builder()
	                    .resourceType(ResourceType.DRIVE)
	                    .ownerEmpId(item.getFrstRgtrId())
	                    .build());
		
		//파일 리소스 가져오기
		Resource resource = fileService.download(item.getDriveAtchFileId());
		
		//파일명 인코딩(한글 파일명 깨짐 방지)
		String encodedFileName;
		try {
			encodedFileName = URLEncoder.encode(item.getOrgnlFileNm(), "UTF-8").replace("+", "%20");
		}catch(UnsupportedEncodingException e) {
			encodedFileName = item.getOrgnlFileNm();
		}
		
		//헤더, 바디 세팅
		return ResponseEntity.ok()
				.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename*=UTF-8''" + encodedFileName)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}



}
