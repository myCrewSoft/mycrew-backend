package com.mycrewsoft.domain.task.mapper;

import com.mycrewsoft.domain.task.dto.request.TaskCreateRequest;
import com.mycrewsoft.domain.task.dto.request.TaskUpdateRequest;
import com.mycrewsoft.domain.task.dto.response.TaskDetailResponse;
import com.mycrewsoft.domain.task.dto.response.TaskListResponse;
import com.mycrewsoft.domain.task.vo.TaskDetailVO;
import com.mycrewsoft.domain.task.vo.TaskListVO;
import com.mycrewsoft.domain.task.vo.TaskPtcptDetailVO;
import com.mycrewsoft.domain.task.vo.TaskPtcptVO;
import com.mycrewsoft.domain.task.vo.TaskVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskDtoMapper {

    /** TaskCreateRequest → TaskVO 변환 (Service에서 frstRgtrId, lastMdfrId, taskStatCd 직접 세팅) */
    @Mapping(target = "taskId",         ignore = true)
    @Mapping(target = "taskStatCd",     ignore = true)
    @Mapping(target = "taskPrgrsSmry",  ignore = true)
    @Mapping(target = "frstRegDt",      ignore = true)
    @Mapping(target = "frstRgtrId",     ignore = true)
    @Mapping(target = "lastMdfcnDt",    ignore = true)
    @Mapping(target = "lastMdfrId",     ignore = true)
    @Mapping(target = "delYn",          ignore = true)
    @Mapping(target = "delDt",          ignore = true)
    @Mapping(target = "deltrMbrId",     ignore = true)
    @Mapping(target = "chtrmId",        ignore = true)
    @Mapping(target = "taskAtchFileId", ignore = true)
    @Mapping(target = "taskEmployeeList", ignore = true)
    TaskVO toVO(TaskCreateRequest request);

    /** TaskUpdateRequest → TaskVO 변환 (projId, taskTypeCd는 수정 대상 아님) */
    @Mapping(target = "taskId",         ignore = true)
    @Mapping(target = "projId",         ignore = true)
    @Mapping(target = "chtrmId",        ignore = true)
    @Mapping(target = "taskTypeCd",     ignore = true)
    @Mapping(target = "frstRegDt",      ignore = true)
    @Mapping(target = "frstRgtrId",     ignore = true)
    @Mapping(target = "lastMdfcnDt",    ignore = true)
    @Mapping(target = "lastMdfrId",     ignore = true)
    @Mapping(target = "delYn",          ignore = true)
    @Mapping(target = "delDt",          ignore = true)
    @Mapping(target = "deltrMbrId",     ignore = true)
    @Mapping(target = "taskAtchFileId", ignore = true)
    @Mapping(target = "taskEmployeeList", ignore = true)
    TaskVO toVO(TaskUpdateRequest request);

    /** empId, taskId → TaskPtcptVO 변환 */
    @Mapping(target = "taskPtcptId", ignore = true)
    @Mapping(target = "joinDt",      ignore = true)
    @Mapping(target = "leaveDt",     ignore = true)
    TaskPtcptVO toPtcptVO(Long taskId, Long empId);

    /** TaskListVO → TaskListResponse 변환 */
    TaskListResponse toListResponse(TaskListVO vo);

    /** List<TaskListVO> → List<TaskListResponse> 변환 */
    List<TaskListResponse> toListResponseList(List<TaskListVO> voList);

    /** TaskPtcptDetailVO → TaskEmployeeResponse 변환 */
    TaskDetailResponse.TaskEmployeeResponse toPtcptResponse(TaskPtcptDetailVO vo);

    /** TaskDetailVO → TaskDetailResponse 변환 (ptcptList → employeeList 필드명 다름) */
    @Mapping(source = "ptcptList", target = "employeeList")
    TaskDetailResponse toDetailResponse(TaskDetailVO vo);
}