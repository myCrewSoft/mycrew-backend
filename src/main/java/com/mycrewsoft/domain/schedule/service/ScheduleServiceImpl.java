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
import com.mycrewsoft.domain.schedule.vo.SchdTargetVO;
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
	
	@Override
	@Transactional
	public Long createSchd(ScheduleRequestDto dto) {
		
		// 사용자 정보 조회
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// 권한 체크
		if(dto.getSchdClsfCd().equals("C001") || dto.getSchdClsfCd().equals("C003")) {
			if(SecurityUtil.isCurrentExec()) {
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
	public void modifySchd(Long schdId, ScheduleRequestDto dto, Long empId) {
		
	}

	@Override
	public void deleteSchd(Long schdId, Long empId) {
		
	}

	@Override
	public ScheduleResponseDto readSchd(Long schdId) {
		return null;
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
	
	//일정 참여자 목록 생성
	private List<SchdTargetVO> buildTargetList(ScheduleRequestDto dto, Long schdId) {

	    List<SchdTargetVO> targets = new ArrayList<>();
	    Long empId = SecurityUtil.getCurrentEmpId();

	    // 전사 일정 - 타겟 행 하나로 전체 의미
	    if ("C001".equals(dto.getSchdClsfCd())) {
	        targets.add(SchdTargetVO.builder()
	                .schdId(schdId).targetTypeCd("01").targetId("0").build());

	    // 개인 일정 - 본인 사번 자동 추가
	    } else if ("C002".equals(dto.getSchdClsfCd())) {
	        targets.add(SchdTargetVO.builder()
	                .schdId(schdId).targetTypeCd("02").targetId(String.valueOf(empId)).build());
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
	        builder.projId(String.valueOf(dto.getTaskId()));
	    } 
	    
	    return builder.build();
	}
}
