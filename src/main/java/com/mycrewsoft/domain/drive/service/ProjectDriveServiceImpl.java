package com.mycrewsoft.domain.drive.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

}
