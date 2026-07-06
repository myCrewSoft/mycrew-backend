package com.mycrewsoft.domain.drive.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import com.mycrewsoft.domain.drive.dto.DriveRenameRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.dto.DriveSearchRequestDto;
import com.mycrewsoft.domain.drive.mapper.DriveMapper;
import com.mycrewsoft.domain.drive.vo.DriveVo;
import com.mycrewsoft.domain.file.constant.FileConstants;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileService;
import com.mycrewsoft.domain.projectmember.mapper.ProjectMemberMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectDriveServiceImpl implements ProjectDriveService{
	
	private final DtoMapper dtoMapper;
	private final DriveMapper mapper;
	private final AuthorizationService authorizationService;
	private final ProjectMemberMapper projMemMapper;
	private final FileService fileService;
	
	// 프로젝트 스코프 권한 컨텍스트
    private ResourceContext buildProjectContext(Long projId) {
        return ResourceContext.builder()
                .resourceType(ResourceType.DRIVE)
                .projId(String.valueOf(projId))
                .build();
    }
    
    //프로젝트 참여자 검증
    private void validateProjectParticipant(Long projId, Long empId) {
    	boolean isMember = projMemMapper.selectProjectMemberList(projId).stream()
    			.anyMatch(m -> m.getEmpId().equals(empId));
    	if(!isMember) {
    		throw new CustomException(ErrorCode.PROJECT_NOT_PARTICIPANT);
    	}
    }
    
	/**
	 * 프로젝트 드라이브 목록 조회
	 */
	@Override
	@Transactional
	public Page<DriveResponseDto> getProjectDriveList(Long projId, DriveSearchRequestDto reqDto) {
		//권한 체크
		authorizationService.assertCurrentUserPermission(
				PermissionCode.PROJECT_DRIVE_READ, buildProjectContext(projId));
		
		//해당 프로젝트 참여자인지 확인
		validateProjectParticipant(projId, SecurityUtil.getCurrentEmpId());
		
		int offset = reqDto.getPage() * reqDto.getSize();
		
		long total = mapper.countDriveListByProjId(projId, reqDto.getPrntDriveItemId());
		List<DriveVo> voList = mapper.selectListByProjId(projId, reqDto.getPrntDriveItemId(), offset, reqDto.getSize());
		
		//vo -> dto
		List<DriveResponseDto> content = voList.stream()
				.map(vo -> {DriveResponseDto dto = dtoMapper.toDto(vo, DriveResponseDto.class);
					dto.setFrstRegDt(DateUtil.format(vo.getFrstRegDt()));
					dto.setLastMdfcnDt(DateUtil.format(vo.getLastMdfcnDt()));
					dto.setFileSz(vo.getFileSz() != null ? FileUtil.formatFileSize(vo.getFileSz()) : null);
					return dto;
				}).collect(Collectors.toList());
		
		PageRequest pageable = PageRequest.of(reqDto.getPage(), reqDto.getSize());
		return new PageImpl<>(content, pageable, total);
	}

	/**
	 * 폴더 생성
	 */
	@Override
	@Transactional
	public void createFolder(Long projId, DriveFolderCreateRequestDto reqDto) {
		//권한체크
		authorizationService.assertCurrentUserPermission(
				PermissionCode.PROJECT_DRIVE_UPLOAD, buildProjectContext(projId));
		
		//해당 프로젝트 참여자인지 확인
		validateProjectParticipant(projId, SecurityUtil.getCurrentEmpId());
		
		//dto -> vo
		DriveVo vo = dtoMapper.toDto(reqDto, DriveVo.class);
		vo.setItemTypeCd("01"); 							//폴더
		vo.setFrstRgtrId(SecurityUtil.getCurrentEmpId()); 	//등록자
		vo.setDriveScopeCd("02");							//프로젝트 드라이브
		vo.setProjId(projId);								//프로젝트 id
		
		//DB에 저장
		int result = mapper.insertDriveItem(vo);
		if(result == 0) throw new CustomException(ErrorCode.DRIVE_INSERT_FAILED);
	}

	@Override
	@Transactional
	public void uploadFile(Long projId, FileUploadRequestDto reqDto, Long prntDriveItemId) {
		//권한 체크
		authorizationService.assertCurrentUserPermission(
				PermissionCode.PROJECT_DRIVE_UPLOAD, buildProjectContext(projId));
		
		//해당 프로젝트 참여자인지 확인
		validateProjectParticipant(projId, SecurityUtil.getCurrentEmpId());
		
		//파일 업로드
		Long driveAtchFileId = fileService.upload(reqDto, FileConstants.DRIVE);
		
		//드라이브에 저장
		DriveVo vo = new DriveVo();
		vo.setDriveAtchFileId(driveAtchFileId);
		vo.setProjId(projId);
		vo.setPrntDriveItemId(prntDriveItemId);					
		vo.setFrstRgtrId(SecurityUtil.getCurrentEmpId());
		vo.setItemTypeCd("02");
		vo.setDriveScopeCd("02");	
		vo.setItemNm(reqDto.getFile().getOriginalFilename());
		
		int result = mapper.insertDriveItem(vo);
		if(result == 0) throw new CustomException(ErrorCode.DRIVE_INSERT_FAILED);
	}

	/**
	 * 프로젝트 드라이브 폴더명 수정
	 */
	@Override
	public void renameItem(Long driveItemId, DriveRenameRequestDto reqDto) {
		//해당 아이템이 존재하는지 확인
		DriveVo vo = mapper.selectDriveItemById(driveItemId);
		if(vo == null) throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		Long currentProjId = vo.getProjId();
		
		//권한체크
		authorizationService.assertCurrentUserPermission(
				PermissionCode.PROJECT_DRIVE_UPDATE, buildProjectContext(currentProjId));
		
		//프로젝트 참여자인지 검증
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		validateProjectParticipant(currentProjId, currentEmpId);
		
		//폴더가 아니면 수정할 수 없음
		if(!"01".equals(vo.getItemTypeCd())) throw new CustomException(ErrorCode.DRIVE_RENAME_NOT_ALLOWED);
		
		//폴더명 수정
		mapper.updateFolderName(driveItemId, reqDto.getItemNm(), currentEmpId);
	}

	/**
	 * 프로젝트 드라이브 아이템 즐겨찾기 등록/해제
	 */
	@Override
	public void toggleBookmark(Long driveItemId) {
		//아이템 존재 여부 확인
		DriveVo vo = mapper.selectDriveItemById(driveItemId);
		if(vo == null) throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		Long currentProjId = vo.getProjId();
		
		//로그인한 사용자가 프로젝트 참여자인지 확인
		validateProjectParticipant(currentProjId, SecurityUtil.getCurrentEmpId());
		
		//즐겨찾기 등록/해제
		String newBookmarkYn = "N".equals(vo.getBookmarkYn()) ? "Y" : "N";
		mapper.updateBookmarkYn(driveItemId, newBookmarkYn, SecurityUtil.getCurrentEmpId());
	}

	/**
	 * 프로젝트 드라이브 아이템 논리 삭제(하위 포함)
	 */
	@Override
	@Transactional
	public void softDeleteItem(Long driveItemId) {
		//아이템 존재 여부 확인
		DriveVo vo = mapper.selectDriveItemById(driveItemId);
		if(vo == null) throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		Long currentProjId = vo.getProjId();
		
		//권한체크
		authorizationService.assertCurrentUserPermission(
				PermissionCode.PROJECT_DRIVE_DELETE, buildProjectContext(currentProjId));
		
		//해당 프로젝트 참여자인지 확인
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		validateProjectParticipant(currentProjId, currentEmpId);
		
		//파일 단건 삭제라면
		if("02".equals(vo.getItemTypeCd())) {
			fileService.deleteFile(vo.getDriveAtchFileId());
		}else {
		//하위에 포함된 파일일 때
		 List<DriveVo> fileChildren = mapper.selectFileChildrenByItemId(driveItemId);
	        for (DriveVo child : fileChildren) {
	            if ("02".equals(child.getItemTypeCd()) && child.getDriveAtchFileId() != null) {
	                fileService.deleteFile(child.getDriveAtchFileId()); 
	            }
	        }
		}
		//드라이브 아이템 삭제여부 수정
		mapper.softDeleteItemWithChildren(driveItemId, SecurityUtil.getCurrentEmpId());
	}

	/**
	 * 프로젝트 드라이브 파일 다운로드
	 */
	@Override
	public ResponseEntity<Resource> downloadFile(Long driveItemId) {
		//해당 아이템 존재 여부 확인
		DriveVo vo = mapper.selectDriveItemById(driveItemId);
		if(vo == null) throw new CustomException(ErrorCode.DRIVE_ITEM_NOT_FOUND);
		
		//아이템이 파일인지 확인
		if(!"02".equals(vo.getItemTypeCd())) throw new CustomException(ErrorCode.DRIVE_NOT_A_FILE);
		
		//로그인한 사용자가 프로젝트 참여자인지 확인
		validateProjectParticipant(vo.getProjId(), SecurityUtil.getCurrentEmpId());
		
		//파일 반환
		Resource downloadFile = fileService.download(vo.getDriveAtchFileId());
		
		//파일명 인코딩(한글 파일명 깨짐 방지)
		String encodedFileName;
		try {
			encodedFileName = URLEncoder.encode(vo.getOrgnlFileNm(), "UTF-8").replace("+", "%20");
		}catch(UnsupportedEncodingException e) {
			encodedFileName = vo.getOrgnlFileNm();
		}
				
		//헤더, 바디 세팅
		return ResponseEntity.ok()
				.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename*=UTF-8''" + encodedFileName)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(downloadFile);
		
	}

}
