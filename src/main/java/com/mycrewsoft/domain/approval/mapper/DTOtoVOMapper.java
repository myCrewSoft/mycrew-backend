package com.mycrewsoft.domain.approval.mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycrewsoft.domain.approval.approvaldocVO.ApprovalDocVO;
import com.mycrewsoft.domain.approval.approvalfileVO.ApprovalFileVO;
import com.mycrewsoft.domain.approval.approvallineVO.ApprovalLineVO;
import com.mycrewsoft.domain.approval.approvalstepVO.ApprovalStepVO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;

@Mapper(componentModel = "spring")
public interface DTOtoVOMapper {

    @Mapping(target = "drftDocSn", ignore = true)
    @Mapping(target = "empId", source = "empId")
    @Mapping(target = "drftReqstDt", source = "drftReqstDt")
    @Mapping(target = "aprvlDocSttsCd", source = "aprvlDocSttsCd")
    @Mapping(target = "aprvlCmptnDt", ignore = true)
    @Mapping(target = "rtrnDt", ignore = true)
    @Mapping(target = "aprvlFullRecCn", ignore = true)
    @Mapping(target = "approvalFile", ignore = true)
    @Mapping(target = "approvalSteps", ignore = true)
    ApprovalDocVO toApprovalDocVO(
            ApprovalDraftRequestDTO request,
            Long empId,
            LocalDateTime drftReqstDt,
            String aprvlDocSttsCd);

    @Mapping(target = "drftDocSn", ignore = true)
    @Mapping(target = "atchFileId", source = "atchFileId")
    ApprovalFileVO toApprovalFileVO(Long atchFileId);

    @Mapping(target = "aprvlStepSn", ignore = true)
    @Mapping(target = "drftDocSn", ignore = true)
    @Mapping(target = "empId", source = "empId")
    @Mapping(target = "reqAprvlCnt", expression = "java(approvalCount(request))")
    @Mapping(target = "stepPrgrsCd", source = "stepPrgrsCd")
    @Mapping(target = "frstRgtrId", source = "empId")
    @Mapping(target = "frstRegDt", source = "frstRegDt")
    @Mapping(target = "lastMdfrId", ignore = true)
    @Mapping(target = "lastMdfcnDt", ignore = true)
    @Mapping(target = "approvalLines", expression = "java(toApprovalLineVOList(request.getAprvrEmpIds(), empId, aprvlPrgrsCd))")
    ApprovalStepVO toApprovalStepVO(
            ApprovalStepRequestDTO request,
            Long empId,
            LocalDateTime frstRegDt,
            String stepPrgrsCd,
            String aprvlPrgrsCd);

    @Mapping(target = "aprvlStepSn", ignore = true)
    @Mapping(target = "drftDocSn", ignore = true)
    @Mapping(target = "aprvlLineSn", ignore = true)
    @Mapping(target = "empId", source = "empId")
    @Mapping(target = "aprvrEmpId", source = "aprvrEmpId")
    @Mapping(target = "aprvlDt", ignore = true)
    @Mapping(target = "aprvlPrgrsCd", source = "aprvlPrgrsCd")
    @Mapping(target = "aprvlRsn", ignore = true)
    @Mapping(target = "rtrnRsn", ignore = true)
    ApprovalLineVO toApprovalLineVO(Long aprvrEmpId, Long empId, String aprvlPrgrsCd);

    default List<ApprovalLineVO> toApprovalLineVOList(
            Collection<Long> aprvrEmpIds,
            Long empId,
            String aprvlPrgrsCd) {
        if (aprvrEmpIds == null) {
            return List.of();
        }
        return aprvrEmpIds.stream()
                .map(aprvrEmpId -> toApprovalLineVO(aprvrEmpId, empId, aprvlPrgrsCd))
                .toList();
    }

    default Long approvalCount(ApprovalStepRequestDTO request) {
        return request.getAprvrEmpIds() == null ? 0L : (long) request.getAprvrEmpIds().size();
    }
}
