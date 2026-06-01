package com.mycrewsoft.domain.schedule.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.mapper.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.schedule.dto.request.ScheduleRequestDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleResponseDto;
import com.mycrewsoft.domain.schedule.mapper.IntgSchdMapper;
import com.mycrewsoft.domain.schedule.mapper.SchdTargetMapper;
import com.mycrewsoft.domain.schedule.mapper.ScheduleMapper;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdSearchVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetDetailVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetVO;
import com.mycrewsoft.domain.task.mapper.TaskMapper;
import com.mycrewsoft.domain.task.vo.TaskVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.PermissionCode;
import com.mycrewsoft.security.authz.PermissionScopeSet;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{

	private final IntgSchdMapper intgSchdMapper;
	private final SchdTargetMapper schdTargetMapper;
	private final ScheduleMapper scheduleMapper;
	private final EmployeeMapper employeeMapper;
	private final AuthorizationService authorizationService;
	private final TaskMapper taskMapper;
	
	@Override
	@Transactional
	public Long createSchd(ScheduleRequestDto dto) {
		
		// 사용자 정보 조회
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// 권한 체크
		if(dto.getSchdClsfCd().equals("C001") || dto.getSchdClsfCd().equals("C003")) {
			if(!SecurityUtil.isCurrentExec()) {
				throw new CustomException(ErrorCode.ACCESS_DENIED);
			}
		} else {
			ResourceContext resource = buildResourceContext(dto);
			authorizationService.assertCurrentUserPermission(PermissionCode.SCHEDULE_CREATE, resource);			
		}
		
		// DTO -> VO 변환
		IntgSchdVO schdVO = scheduleMapper.toVo(dto, empId);
						
		// 일정 등록
		intgSchdMapper.insertIntgSchd(schdVO);
		
		// 타겟 목록 구성 및 등록
		List<SchdTargetVO> targets = buildTargetList(dto, schdVO.getSchdId());
		if(!targets.isEmpty()) schdTargetMapper.insertSchdTargetList(targets);
		
		return schdVO.getSchdId();
	}

	@Override
	@Transactional
	public ScheduleResponseDto readSchd(Long schdId) {
		// 권한 체크
		ResourceContext resource = ResourceContext.builder()
				.resourceType(ResourceType.SCHEDULE)
				.build();
		authorizationService.assertCurrentUserPermission(
				PermissionCode.SCHEDULE_READ,
				resource);
		
		// 일정 조회
		IntgSchdVO schdVO = intgSchdMapper.selectIntgSchd(schdId);
		if (schdVO == null) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);
		
		// 본인 체크
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		if(currentEmpId == null || !currentEmpId.equals(schdVO.getSchdWrtrId())) {
			throw new CustomException(ErrorCode.NOT_SCHEDULE_OWNER);
		}
		
		// 공유 대상 상세 조회
	    List<SchdTargetDetailVO> targets = schdTargetMapper.selectSchdTargetDetail(schdId);

		// vo -> dto		
		return scheduleMapper.toResponseDto(schdVO, targets);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ScheduleResponseDto> readSchdList(LocalDateTime beginDt, LocalDateTime endDt) {
		
		// 권한 체크
		ResourceContext resource = ResourceContext.builder()
				.resourceType(ResourceType.SCHEDULE)
				.build();
		authorizationService.assertCurrentUserPermission(
				PermissionCode.SCHEDULE_READ,
				resource);
		
		//  사용자의 정보 조회
		Long empId = SecurityUtil.getCurrentEmpId();
		Boolean exec = SecurityUtil.isCurrentExec();
		String deptCd = employeeMapper.selectEmpDeptCodeByEmpId(empId);
		
		// 조회 조건 객체 생성
		SchdSearchVO searchVO = SchdSearchVO.builder()
				.empId(empId)
				.deptCd(deptCd)
				.execYn(exec)
				.projIds(null)
				.taskIds(null)
				.beginDt(beginDt)
				.endDt(endDt)
				.build();
		
		// 일정 조회
		List<IntgSchdVO> schdList = intgSchdMapper.selectIntgSchdList(searchVO);
		
		return scheduleMapper.toDtoList(schdList);
	}
	
	@Override
	@Transactional
	public void modifySchd(Long schdId, ScheduleRequestDto dto) {
		// 일정 조회
		IntgSchdVO schdVO = intgSchdMapper.selectIntgSchd(schdId);
		if (schdVO == null) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);
		
		// 권한 체크
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		if(currentEmpId == null || !currentEmpId.equals(schdVO.getSchdWrtrId())) {
			throw new CustomException(ErrorCode.NOT_SCHEDULE_OWNER);
		}
		
		// 일정 VO -> DTO 변환
		IntgSchdVO updateVO = scheduleMapper.toVo(dto, currentEmpId);
		updateVO.setSchdId(schdId);
		updateVO.setSchdChgrId(currentEmpId);
		
		int updated = intgSchdMapper.updateIntgSchd(updateVO);
		if(updated == 0) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);
		
	    // 5. 공유 대상 수정 (기존 전체 삭제 → 재등록)
	    schdTargetMapper.deleteSchdTarget(schdId);
	    
	    List<SchdTargetVO> targets = buildTargetList(dto, schdId);
	    if (!targets.isEmpty()) {
	        schdTargetMapper.insertSchdTargetList(targets);
	    }
	}

	@Override
	@Transactional
	public void deleteSchd(Long schdId) {
		// 일정 조회
		IntgSchdVO schdVO = intgSchdMapper.selectIntgSchd(schdId);
		if (schdVO == null) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);
		
		// 권한 체크
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		if(currentEmpId == null || !currentEmpId.equals(schdVO.getSchdWrtrId())) {
			throw new CustomException(ErrorCode.NOT_SCHEDULE_OWNER);
		}
		
		// 공유 대상 삭제
	    schdTargetMapper.deleteSchdTarget(schdId);
	    
	    // 일정 삭제
		int deleted = intgSchdMapper.deleteIntgSchd(schdId);
		if(deleted == 0) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);
	}
	
	//일정 참여자 목록 생성
	private List<SchdTargetVO> buildTargetList(ScheduleRequestDto dto, Long schdId) {

	    List<SchdTargetVO> targets = new ArrayList<>();
	    Long empId = SecurityUtil.getCurrentEmpId();

	    // 전사 일정 - 타겟 행 하나로 전체 의미
	    if ("C001".equals(dto.getSchdClsfCd())) {
	        targets.add(SchdTargetVO.builder()
	                .schdId(schdId).targetTypeCd("01").targetId("0").build());
	    } else if ("C002".equals(dto.getSchdClsfCd())) {
	    	// 개인 일정 - 본인 사번 자동 추가
	        targets.add(SchdTargetVO.builder()
	                .schdId(schdId).targetTypeCd("02").targetId(String.valueOf(empId)).build());
	    } else if ("C004".equals(dto.getSchdClsfCd())) {
	    	// 부서 일정
	    	targets.add(SchdTargetVO.builder()
	    			.schdId(schdId).targetTypeCd("04").targetId(dto.getDeptCd()).build());
	    } else if ("C005".equals(dto.getSchdClsfCd())) {
	    	// 프로젝트 일정
	    	targets.add(SchdTargetVO.builder()
	    			.schdId(schdId).targetTypeCd("05").targetId(String.valueOf(dto.getProjId())).build());
	    } else if ("C006".equals(dto.getSchdClsfCd())) {
	    	// 업무 일정
	    	targets.add(SchdTargetVO.builder()
	    			.schdId(schdId).targetTypeCd("06").targetId(String.valueOf(dto.getTaskId())).build());
	    }
	    
	    // 프론트에서 선택한 targets 그대로 사용
	    if (dto.getTargets() != null && !dto.getTargets().isEmpty()) {
	        targets.addAll(scheduleMapper.toTargetVoList(dto.getTargets(), schdId));
	    }

	    return targets;
	}
	
	// 권한 ResourceContext 체크
	private ResourceContext buildResourceContext(ScheduleRequestDto dto) {

	    ResourceContext.ResourceContextBuilder builder = ResourceContext.builder()
	            .resourceType(ResourceType.SCHEDULE);
	    
	    if ("C002".equals(dto.getSchdClsfCd())) {
	    	builder.ownerEmpId(SecurityUtil.getCurrentEmpId());
	    } else if ("C004".equals(dto.getSchdClsfCd())) {
	        builder.deptCd(dto.getDeptCd());
	    } else if ("C005".equals(dto.getSchdClsfCd())) {
	        builder.projId(String.valueOf(dto.getProjId()));
	    } else if ("C006".equals(dto.getSchdClsfCd())) {
	    	TaskVO task = taskMapper.selectTaskAuthContextByTaskId(dto.getTaskId());
	    	if (task == null) {
	    		throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
	    	}

	        builder
	        	.resourceId(String.valueOf(dto.getTaskId()))
	        	.taskId(String.valueOf(dto.getTaskId()))
	        	.projId(String.valueOf(task.getProjId()))
	        	.ownerEmpId(task.getTaskMngrId());
	    } 
	    
	    return builder.build();
	}
}
