	package com.mycrewsoft.domain.schedule.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.holiday.mapper.HolidayMapper;
import com.mycrewsoft.domain.holiday.vo.HolidayVO;
import com.mycrewsoft.domain.schedule.dto.command.MeetingScheduleCreateCommand;
import com.mycrewsoft.domain.schedule.dto.command.ProjectScheduleCreateCommand;
import com.mycrewsoft.domain.schedule.dto.command.TaskScheduleCreateCommand;
import com.mycrewsoft.domain.schedule.dto.request.ScheduleRequestDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleResponseDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleWidgetItemResponse;
import com.mycrewsoft.domain.schedule.mapper.IntgSchdMapper;
import com.mycrewsoft.domain.schedule.mapper.SchdTargetMapper;
import com.mycrewsoft.domain.schedule.mapper.ScheduleDtoMapper;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdSearchVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetDetailVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetVO;
import com.mycrewsoft.domain.schedule.vo.SchdWidgetVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{

	private final IntgSchdMapper intgSchdMapper;
	private final SchdTargetMapper schdTargetMapper;
	private final ScheduleDtoMapper scheduleMapper;
	private final EmployeeMapper employeeMapper;
	private final HolidayMapper holidayMapper;
	private final AuthorizationService authorizationService;
	
	private static final int WIDGET_SCHD_LIMIT = 3;
	
	// 사용자가 직접 등록
	@Override
	@Transactional
	public Long createSchd(ScheduleRequestDto dto) {
		
		// 사용자 정보 조회
		Long empId = SecurityUtil.getCurrentEmpId();
		
		// 자동 생성 검증
		validateManualScheduleType(dto.getSchdClsfCd());
		
		// 권한 체크
	
		// DTO -> VO 변환
		IntgSchdVO schdVO = scheduleMapper.toVo(dto, empId);
						
		// 일정 등록
		Long schdId = insertSchedule(schdVO);
		
		// 타겟 목록 구성 및 등록
		saveScheduleTargets(dto, schdId);
		
		return schdId;
	}

	// 프로젝트 일정 자동 생성
	@Override
	@Transactional
	public Long createProjectSchedule(ProjectScheduleCreateCommand command) {

		if (command.getProjId() == null) {
	        throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
	    }
		
		if (command.getProjBgngYmd() == null || command.getProjEndYmd() == null) {
		    throw new CustomException(ErrorCode.PROJECT_INVALID_DATE);
		}
		
	    IntgSchdVO schdVO = new IntgSchdVO();

	    // 일정 기본 정보
	    schdVO.setSchdNm("[프로젝트] " + command.getProjNm());
	    schdVO.setSchdDetailCn(command.getProjNm() + " 프로젝트 일정입니다.");
	    schdVO.setSchdClsfCd("C005"); // 프로젝트 일정 코드 - 실제 공통코드에 맞게 변경
	    schdVO.setAllDayYn("Y");
	    schdVO.setBeginDt(command.getProjBgngYmd().atStartOfDay());
	    schdVO.setEndDt(command.getProjEndYmd().atTime(23, 59, 59));
	    schdVO.setSchdWrtrId(command.getCrtrId());
	    schdVO.setDelYn("N");
	    schdVO.setReptYn("N");

	    // 일정 등록
	    Long schdId = insertSchedule(schdVO);

	    // 프로젝트 일정 타겟 등록
	    List<SchdTargetVO> targets = List.of(
	        SchdTargetVO.builder()
	            .schdId(schdId)
	            .targetTypeCd("05")
	            .targetId(String.valueOf(command.getProjId()))
	            .build()
	    );

	    saveScheduleTargets(targets);

	    return schdId;
	}
	
	// 업무 일정 자동 생성
	@Override
	@Transactional
	public Long createTaskSchedule(TaskScheduleCreateCommand command) {

	    if (command.getTaskId() == null) {
	        throw new CustomException(ErrorCode.TASK_NOT_FOUND);
	    }

	    if (command.getTaskBgngYmd() == null || command.getTaskEndYmd() == null) {
	        throw new CustomException(ErrorCode.TASK_INVALID_DATE);
	    }
	    
	    IntgSchdVO schdVO = new IntgSchdVO();

	    // 일정 기본 정보
	    schdVO.setSchdNm("[업무] " + command.getTaskNm());
	    schdVO.setSchdDetailCn(command.getTaskNm() + " 업무 일정입니다.");
	    schdVO.setSchdClsfCd("C006");
	    schdVO.setAllDayYn("Y");
	    schdVO.setBeginDt(command.getTaskBgngYmd());
	    schdVO.setEndDt(command.getTaskEndYmd());
	    schdVO.setSchdWrtrId(command.getCrtrId());
	    schdVO.setDelYn("N");
	    schdVO.setReptYn("N");

	    // 일정 등록
	    Long schdId = insertSchedule(schdVO);

	    // 업무 일정 타겟 등록
	    List<SchdTargetVO> targets = List.of(
	        SchdTargetVO.builder()
	            .schdId(schdId)
	            .targetTypeCd("06")
	            .targetId(String.valueOf(command.getTaskId()))
	            .build()
	    );

	    saveScheduleTargets(targets);

	    return schdId;
	}
	
	// 화상회의 일정 자동 생성
	@Override
	@Transactional
	public Long createMeetingSchedule(MeetingScheduleCreateCommand command) {

	    if (command.getMeetingId() == null) {
	        throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);
	    }

	    if (command.getBeginDt() == null || command.getEndDt() == null) {
	        throw new CustomException(ErrorCode.VIDEO_CONF_INVALID_DATE);
	    }
	    
	    IntgSchdVO schdVO = new IntgSchdVO();

	    // 일정 기본 정보
	    schdVO.setSchdNm("[회의] " + command.getMeetingNm());
	    schdVO.setSchdDetailCn(command.getMeetingNm() + " 회의 일정입니다.");
	    schdVO.setSchdClsfCd("C007");
	    schdVO.setAllDayYn("N");
	    schdVO.setBeginDt(command.getBeginDt());
	    schdVO.setEndDt(command.getEndDt());
	    schdVO.setSchdWrtrId(command.getCrtrId());
	    schdVO.setDelYn("N");
	    schdVO.setReptYn("N");

	    // 일정 등록
	    Long schdId = insertSchedule(schdVO);

	    // 회의 일정 타겟 등록
	    List<SchdTargetVO> targets = List.of(
	        SchdTargetVO.builder()
	            .schdId(schdId)
	            .targetTypeCd("07")
	            .targetId(String.valueOf(command.getMeetingId()))
	            .build()
	    );

	    saveScheduleTargets(targets);

	    return schdId;
	}
	
	@Override
	@Transactional
	public ScheduleResponseDto readSchd(Long schdId) {
	    // 권한 체크
	    ResourceContext resource = ResourceContext.builder()
	            .resourceType(ResourceType.SCHEDULE)
	            .build();
	    authorizationService.assertCurrentUserPermission(PermissionCode.SCHEDULE_READ, resource);

	    // 일정 조회
	    IntgSchdVO schdVO = intgSchdMapper.selectIntgSchd(schdId);
	    if (schdVO == null) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);

	    // 열람 권한 체크 (작성자이거나 공유 대상에 포함되는지)
	    Long currentEmpId = SecurityUtil.getCurrentEmpId();
	    Boolean exec = SecurityUtil.isCurrentExec();
	    String deptCd = employeeMapper.selectEmpDeptCodeByEmpId(currentEmpId);

	    if (!canViewSchd(schdVO, currentEmpId, exec, deptCd)) {
	        throw new CustomException(ErrorCode.ACCESS_DENIED);
	    }

	    // 공유 대상 상세 조회
	    List<SchdTargetDetailVO> targets = schdTargetMapper.selectSchdTargetDetail(schdId);

	    return scheduleMapper.toResponseDto(schdVO, targets);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ScheduleResponseDto> readSchdList(LocalDateTime beginDt, LocalDateTime endDt) {

	    // 권한 체크
	    ResourceContext resource = ResourceContext.builder()
	            .resourceType(ResourceType.SCHEDULE)
	            .build();
	    authorizationService.assertCurrentUserPermission(PermissionCode.SCHEDULE_READ, resource);

	    // 사용자 정보 조회
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
	    List<ScheduleResponseDto> schdDtoList = new ArrayList<>(
	            scheduleMapper.toDtoList(intgSchdMapper.selectIntgSchdList(searchVO))
	    );

	    // 공휴일 조회 후 변환 및 합치기
	    int beginYear = beginDt.getYear();
	    int endYear = endDt.getYear();

	    for (int year = beginYear; year <= endYear; year++) {
	        List<HolidayVO> holidays = holidayMapper.selectHolidayList(year);
	        holidays.stream()
	                .filter(h -> {
	                    LocalDateTime holidayDt = h.getHolidayDt().atStartOfDay();
	                    return !holidayDt.isBefore(beginDt) && !holidayDt.isAfter(endDt);
	                })
	                .map(h -> ScheduleResponseDto.builder()
	                        .scheduleTypeCode("Y".equals(h.getIsHolidayYn()) ? "PUBLIC_HOLIDAY" : "ANNIVERSARY")
	                        .title(h.getHolidayNm())
	                        .start(h.getHolidayDt().atStartOfDay())
	                        .end(h.getHolidayDt().atTime(23, 59, 59))
	                        .allDay(true)
	                        .repeat(false)
	                        .build())
	                .forEach(schdDtoList::add);
	    }

	    // 시작일 기준 정렬
	    schdDtoList.sort(Comparator.comparing(ScheduleResponseDto::getStart,
	            Comparator.nullsLast(Comparator.naturalOrder())));

	    return schdDtoList;
	}
	
	@Override
	@Transactional
	public void modifySchd(Long schdId, ScheduleRequestDto dto) {
		// 일정 조회
		IntgSchdVO schdVO = intgSchdMapper.selectIntgSchd(schdId);
		if (schdVO == null) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);
		
		// 자동 생성 검증
		validateManualScheduleType(dto.getSchdClsfCd());
		
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
	    
	    saveScheduleTargets(dto, schdId);
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
	
	@Override
	@Transactional(readOnly = true)
	public List<ScheduleWidgetItemResponse> readTodaySchdListForWidget() {
	    Long empId = SecurityUtil.getCurrentEmpId();
	    Boolean exec = SecurityUtil.isCurrentExec();
	    String deptCd = employeeMapper.selectEmpDeptCodeByEmpId(empId);

	    List<SchdWidgetVO> voList = intgSchdMapper.selectTodaySchdListForWidget(
	            empId,
	            deptCd,
	            exec,
	            DateUtil.startOfToday(),
	            DateUtil.endOfToday(),
	            WIDGET_SCHD_LIMIT
	    );

	    return scheduleMapper.toWidgetItemResponseList(voList);
	}

	// 관리자 대시보드 위젯용: 오늘 + 다가오는 전사(C001)·간부(C003) 중요 일정
	@Override
	@Transactional(readOnly = true)
	public List<ScheduleWidgetItemResponse> readImportantSchdListForAdminWidget(int limit) {
	    // 권한은 호출 측(관리자 대시보드 서비스)에서 ADMIN_CONSOLE_ACCESS로 검증한다.
	    int safeLimit = limit > 0 ? limit : WIDGET_SCHD_LIMIT;

	    List<SchdWidgetVO> voList = intgSchdMapper.selectImportantSchdListForAdminWidget(
	            DateUtil.startOfToday(),
	            safeLimit
	    );

	    return scheduleMapper.toWidgetItemResponseList(voList);
	}

	//일정 참여자 목록 생성
	private List<SchdTargetVO> buildScheduleTargets(ScheduleRequestDto dto, Long schdId) {

	    List<SchdTargetVO> targets = new ArrayList<>();
	    Long empId = SecurityUtil.getCurrentEmpId();

	    // 전사 일정 - 타겟 행 하나로 전체 의미(대상자 null)
	    if ("C001".equals(dto.getSchdClsfCd())) {
	        targets.add(SchdTargetVO.builder()
	                .schdId(schdId).targetTypeCd("01").targetId("0").build());
	    } else if ("C002".equals(dto.getSchdClsfCd())) {
	    	// 개인 일정 - 본인 사번 자동 추가
	        targets.add(SchdTargetVO.builder()
	                .schdId(schdId).targetTypeCd("02").targetId(String.valueOf(empId)).build());
	    } else if ("C003".equals(dto.getSchdClsfCd())) {
	    	// 간부 일정 - 타겟 하나로 간부 전체 의미(대상자 null)
	        targets.add(SchdTargetVO.builder()
	                .schdId(schdId).targetTypeCd("03").targetId("0").build());
	    } else if ("C004".equals(dto.getSchdClsfCd())) {
	    	// 부서 일정(타겟은 부서 Cd)
	    	targets.add(SchdTargetVO.builder()
	    			.schdId(schdId).targetTypeCd("04").targetId(dto.getDeptCd()).build());
	    }
	    
	    // 프론트에서 선택한 targets 그대로 사용
	    if (dto.getTargets() != null && !dto.getTargets().isEmpty()) {
	        targets.addAll(scheduleMapper.toTargetVoList(dto.getTargets(), schdId));
	    }

	    return targets;
	}
	
	// 일정 생성 insert문 메서드
	private Long insertSchedule(IntgSchdVO schdVO) {
	    intgSchdMapper.insertIntgSchd(schdVO);

	    Long schdId = schdVO.getSchdId();

	    if (schdId == null) {
	        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
	    }

	    return schdId;
	}
	
	// 일정 대상자 insert 메서드
	private void saveScheduleTargets(ScheduleRequestDto dto, Long schdId) {
	    List<SchdTargetVO> targets = buildScheduleTargets(dto, schdId);

	    if (targets.isEmpty()) {
	        return;
	    }

	    schdTargetMapper.insertSchdTargetList(targets);
	}
	
	// 자동 일정 대상 등록
	private void saveScheduleTargets(List<SchdTargetVO> targets) {
	    if (targets == null || targets.isEmpty()) {
	        return;
	    }

	    schdTargetMapper.insertSchdTargetList(targets);
	}
	
	// 자동생성 검증
	private void validateManualScheduleType(String schdClsfCd) {
	    if ("C005".equals(schdClsfCd)
	            || "C006".equals(schdClsfCd)
	            || "C007".equals(schdClsfCd)
	            || "C008".equals(schdClsfCd)) {
	        throw new CustomException(ErrorCode.ACCESS_DENIED);
	    }
	}
	
	// 읽기 권한 체크
	private boolean canViewSchd(IntgSchdVO schdVO, Long empId, Boolean exec, String deptCd) {
	    // 1. 작성자 본인
	    if (empId.equals(schdVO.getSchdWrtrId())) return true;

	    // 2. 공유 대상에 포함되는지 확인
	    List<SchdTargetVO> targets = schdVO.getTargets();
	    if (targets == null) return false;

	    for (SchdTargetVO target : targets) {
	        switch (target.getTargetTypeCd()) {
	            case "01" -> { return true; }  // 전사
	            case "02" -> { if (target.getTargetId().equals(String.valueOf(empId))) return true; }  // 개인
	            case "03" -> { if (Boolean.TRUE.equals(exec)) return true; }  // 간부
	            case "04" -> { if (target.getTargetId().equals(deptCd)) return true; }  // 부서
	        }
	    }
	    return false;
	}
	
}
