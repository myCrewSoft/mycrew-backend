package com.mycrewsoft.domain.project.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.messenger.service.MsngrServiceImpl;
import com.mycrewsoft.domain.project.dto.ProjectCreateRequestDto;
import com.mycrewsoft.domain.project.dto.ProjectDetailResponseDto;
import com.mycrewsoft.domain.project.dto.ProjectListResponseDto;
import com.mycrewsoft.domain.project.dto.ProjectUpdateRequestDto;
import com.mycrewsoft.domain.project.event.ProjectCompletedEvent;
import com.mycrewsoft.domain.project.event.ProjectCreatedEvent;
import com.mycrewsoft.domain.project.event.ProjectStoppedEvent;
import com.mycrewsoft.domain.project.mapper.ProjectMapper;
import com.mycrewsoft.domain.project.vo.ProjectVO;
import com.mycrewsoft.domain.projectmember.dto.ProjectMemberResponseDto;
import com.mycrewsoft.domain.projectmember.mapper.ProjectMemberMapper;
import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService{
	private final AuthorizationService authorizationService;
	private final ProjectMapper projectMapper;
	private final ProjectMemberMapper projectMemberMapper;
	private final DtoMapper dtoMapper;
	private final MsngrServiceImpl msngrService;
	private final ApplicationEventPublisher eventPublisher;
	/**
	 * 프로젝트 등록
	 */
	@Override
	@Transactional
	public void createProject(ProjectCreateRequestDto reqDto) {
		 // 권한 체크
	    authorizationService.assertCurrentUserPermission(
	        PermissionCode.PROJECT_CREATE,
	        ResourceContext.builder()
	            .resourceType(ResourceType.PROJECT)
	            .build()
	    );
		
		//종료날짜 > 시작날짜 검증
		LocalDate projBgngYmd = reqDto.getProjBgngYmd();
		LocalDate projEndYmd = reqDto.getProjEndYmd();
		if(!projEndYmd.isAfter(projBgngYmd)) {
			throw new CustomException(ErrorCode.PROJECT_INVALID_DATE);
		}
		
		//dto -> vo 변환
		ProjectVO projVo = dtoMapper.toDto(reqDto, ProjectVO.class);
		projVo.setProjLdrEmpId(SecurityUtil.getCurrentEmpId());
		//채팅방 자동생성 고려
		
		//프로젝트 등록
		int result = projectMapper.insertProject(projVo);
		if(result == 0) throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		
		//참여자 vo 리스트 생성 (projId 세팅)
		List<ProjectMemberVO> memberList = reqDto.getProjMemberList().stream()
                .map(m -> {
                    ProjectMemberVO memberVO = dtoMapper.toDto(m, ProjectMemberVO.class);
                    memberVO.setProjId(projVo.getProjId());
                    return memberVO;
                })
                .toList();
		
		//참여자 일괄 등록
		int memberResult = projectMemberMapper.insertProjectMemberList(memberList);
		if(memberResult == 0) throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		
		// 프로젝트 배정 알림
		List<Long> memberEmpIds = memberList.stream()
		        .map(ProjectMemberVO::getEmpId)
		        .toList();
        eventPublisher.publishEvent(
            new ProjectCreatedEvent(projVo.getProjNm(), memberEmpIds)
        );
	}

	/**
	 * 프로젝트 전체 목록 조회(본인 참여 프로젝트 목록)
	 */
	@Override
	public List<ProjectListResponseDto> getProjectList() {
		//권한 체크
		authorizationService.assertCurrentUserPermission(
				PermissionCode.PROJECT_READ,
				ResourceContext.builder()
					.resourceType(ResourceType.PROJECT)
					.build()
		);
		
		//본인이 참여하는 프로젝트만 조회
		Long empId = SecurityUtil.getCurrentEmpId();
		List<ProjectVO> voList = projectMapper.selectProjectList(empId);
		
		return dtoMapper.toDtoList(voList, ProjectListResponseDto.class);
	}

	/**
	 * 매일 자정 예정 → 진행 중 상태 자동 전환 (배치용)
	 */
	@Override
	@Scheduled(cron = "0 0 0 * * *")
	public void updateProjStateToInProgress() {
		try {
            int count = projectMapper.updateProjStateToInProgress();
            log.info("[ProjectStatScheduler] 예정 → 진행 중 전환 완료: {}건", count);
        } catch (Exception e) {
            log.error("[ProjectStatScheduler] 배치 실패: {}", e.getMessage(), e);
            // TODO: 슬랙 또는 메일 알림 추가
        }
	}
	
	/**
	 * 프로젝트 상세 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public ProjectDetailResponseDto getProject(Long projId) {
		
		Long empId = SecurityUtil.getCurrentEmpId();
		
		//조회 및 참여자 검증 동시 처리
		ProjectVO vo = projectMapper.selectProject(projId, empId);
		if(vo == null) {
			throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
		}
		
		//vo -> dto 변환
		ProjectDetailResponseDto dto = dtoMapper.toDto(vo, ProjectDetailResponseDto.class);
		dto.setProjMemberList(dtoMapper.toDtoList(vo.getProjMemberList(), ProjectMemberResponseDto.class));
		return dto;
	}

	/**
	 * 프로젝트 수정
	 */
	@Override
	public void modifyProject(Long projId, ProjectUpdateRequestDto updateReqDto) {
		// 1. 권한 체크
		authorizationService.assertCurrentUserPermission(
		    PermissionCode.PROJECT_UPDATE,
		    ResourceContext.builder()
		        .resourceType(ResourceType.PROJECT)
		        .resourceId(String.valueOf(projId))
		        .build()
		);
		
		Long empId = SecurityUtil.getCurrentEmpId();
		
		//프로젝트 존재 여부 확인
		ProjectVO vo = projectMapper.selectProject(projId, empId);
		if(vo == null) {
			throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
		}
		
		//프로젝트 장 검증
		if(!vo.getProjLdrEmpId().equals(empId)) {
			throw new CustomException(ErrorCode.PROJECT_NOT_OWNER);
		}
		
		//상태코드 완료('03') 및 중단('04')면 날짜 수정 불가
		String currentStat = vo.getProjStatCd();
		if("03".equals(currentStat) || "04".equals(currentStat)) {
			if(updateReqDto.getProjBgngYmd() != null || updateReqDto.getProjEndYmd() != null) {
				throw new CustomException(ErrorCode.PROJECT_INVALID_STAT_TRANSITION);
			}
		}
		
		//진행 중 상태이면 시작일 수정 불가
		if("02".equals(currentStat) && updateReqDto.getProjBgngYmd() != null) {
			throw new CustomException(ErrorCode.PROJECT_INVALID_STAT_TRANSITION);
		}
		
		//날짜 검증
		if(updateReqDto.getProjBgngYmd() != null && updateReqDto.getProjEndYmd() != null) {
			if(!updateReqDto.getProjBgngYmd().isBefore(updateReqDto.getProjEndYmd())) {
				throw new CustomException(ErrorCode.PROJECT_INVALID_DATE);
			}
		}
		
		//상태 전이 규칙 검증
		if(updateReqDto.getProjStatCd() != null) {
			validateStatTransition(currentStat, updateReqDto.getProjStatCd());
		}
		
		//dto -> vo로 변환
		//예정 상태에서 시작일 변경 시 상태 자동 재계산
		ProjectVO updateVo = dtoMapper.toDto(updateReqDto, ProjectVO.class);
		updateVo.setProjId(projId);
		
		if("01".equals(currentStat) && updateReqDto.getProjBgngYmd() != null) {
			if (!updateReqDto.getProjBgngYmd().isAfter(LocalDate.now())) {
	            updateVo.setProjStatCd("02");  // 진행중으로 자동 전환
	        } else {
	            updateVo.setProjStatCd("01");  // 예정 유지
	        }
		}
		
		//수정
	    int result = projectMapper.updateProject(updateVo);
	    if (result == 0) throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
	    
	    // 상태 변경 시 알림
	    if (updateReqDto.getProjStatCd() != null &&
	        !updateReqDto.getProjStatCd().equals(currentStat)) {

	        List<Long> memberEmpIds = projectMemberMapper
	                .selectProjectMemberList(projId).stream()
	                .map(ProjectMemberVO::getEmpId)
	                .toList();

	        switch (updateReqDto.getProjStatCd()) {
	            case "03" -> eventPublisher.publishEvent(
	                new ProjectCompletedEvent(vo.getProjNm(), memberEmpIds));
	            case "04" -> eventPublisher.publishEvent(
	                new ProjectStoppedEvent(vo.getProjNm(), memberEmpIds));
	        }
	    }
	}
	
	/**
	 * 상태 전이 규칙 검증
	 */
	private void validateStatTransition(String currentStat, String newStat) {
	    // 완료(03) → 변경 불가
	    if ("03".equals(currentStat)) {
	        throw new CustomException(ErrorCode.PROJECT_INVALID_STAT_TRANSITION);
	    }
	    // 중단(04) → 변경 불가
	    if ("04".equals(currentStat)) {
	        throw new CustomException(ErrorCode.PROJECT_INVALID_STAT_TRANSITION);
	    }
	    // 진행중(02) → 예정(01) 불가
	    if ("02".equals(currentStat) && "01".equals(newStat)) {
	        throw new CustomException(ErrorCode.PROJECT_INVALID_STAT_TRANSITION);
	    }
	}
}
