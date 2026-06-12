package com.mycrewsoft.domain.approval.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.approval.approvaldocVO.ApprovalDocVO;
import com.mycrewsoft.domain.approval.approvaltemplateVO.ApprovalTemplateVO;
import com.mycrewsoft.domain.approval.approvalfileVO.ApprovalFileVO;
import com.mycrewsoft.domain.approval.approvallineVO.ApprovalLineVO;
import com.mycrewsoft.domain.approval.approvalstepVO.ApprovalStepVO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDocumentDetailResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftSummaryResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalStepStatusResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.approval.vo.ApprovalDeadlineVO;

@Mapper
public interface ApprovalDraftMapper {

    void insertApprovalDoc(ApprovalDocVO approvalDoc);

    int updateApprovalDocTemporary(ApprovalDocVO approvalDoc);

    void insertApprovalFile(ApprovalFileVO approvalFile);

    void deleteApprovalFileByDocSn(@Param("drftDocSn") Long drftDocSn);

    void insertApprovalStep(ApprovalStepVO approvalStep);

    void insertApprovalLine(ApprovalLineVO approvalLine);

    void deleteApprovalLinesByDocSn(@Param("drftDocSn") Long drftDocSn);

    void deleteApprovalStepsByDocSn(@Param("drftDocSn") Long drftDocSn);

    ApprovalDocVO selectApprovalDocByDocSn(@Param("drftDocSn") Long drftDocSn);

    ApprovalDocVO selectApprovalDocByDocSnForUpdate(@Param("drftDocSn") Long drftDocSn);

    int countApprovalSteps(@Param("drftDocSn") Long drftDocSn);

    int countApprovalLines(@Param("drftDocSn") Long drftDocSn);

    ApprovalStepVO selectFirstApprovalStep(@Param("drftDocSn") Long drftDocSn);

    ApprovalStepVO selectCurrentApprovalStep(@Param("drftDocSn") Long drftDocSn);

    ApprovalStepVO selectNextApprovalStep(
            @Param("drftDocSn") Long drftDocSn,
            @Param("aprvlOrd") Long aprvlOrd);

    ApprovalLineVO selectApprovalLineInStep(
            @Param("aprvlStepSn") Long aprvlStepSn,
            @Param("aprvrEmpId") Long aprvrEmpId);

    List<Long> selectApproverEmpIdsByStep(@Param("aprvlStepSn") Long aprvlStepSn);

    int countApprovalLinesByStepAndStatus(
            @Param("aprvlStepSn") Long aprvlStepSn,
            @Param("aprvlPrgrsCd") String aprvlPrgrsCd);

    int countProcessedApprovalLines(@Param("drftDocSn") Long drftDocSn);

    int updateAllStepsStatus(
            @Param("drftDocSn") Long drftDocSn,
            @Param("stepPrgrsCd") String stepPrgrsCd,
            @Param("empId") Long empId,
            @Param("now") LocalDateTime now);

    int updateAllLinesWaiting(
            @Param("drftDocSn") Long drftDocSn,
            @Param("aprvlPrgrsCd") String aprvlPrgrsCd);

    int updateStepStatus(
            @Param("aprvlStepSn") Long aprvlStepSn,
            @Param("stepPrgrsCd") String stepPrgrsCd,
            @Param("empId") Long empId,
            @Param("now") LocalDateTime now);

    int updateApprovalLineApproved(
            @Param("aprvlLineSn") Long aprvlLineSn,
            @Param("aprvlRsn") String aprvlRsn,
            @Param("aprvrStampFileId") Long aprvrStampFileId,
            @Param("now") LocalDateTime now);

    int updateApprovalLineRejected(
            @Param("aprvlLineSn") Long aprvlLineSn,
            @Param("rtrnRsn") String rtrnRsn,
            @Param("now") LocalDateTime now);

    int updateWaitingLinesInStepToSkipped(
            @Param("aprvlStepSn") Long aprvlStepSn,
            @Param("aprvlPrgrsCd") String aprvlPrgrsCd);

    int updateLaterStepsToSkipped(
            @Param("drftDocSn") Long drftDocSn,
            @Param("aprvlOrd") Long aprvlOrd,
            @Param("stepPrgrsCd") String stepPrgrsCd,
            @Param("empId") Long empId,
            @Param("now") LocalDateTime now);

    int updateWaitingLinesAfterStepToSkipped(
            @Param("drftDocSn") Long drftDocSn,
            @Param("aprvlOrd") Long aprvlOrd,
            @Param("aprvlPrgrsCd") String aprvlPrgrsCd);

    int updateDocumentSubmitted(
            @Param("drftDocSn") Long drftDocSn,
            @Param("aprvlDocSttsCd") String aprvlDocSttsCd,
            @Param("now") LocalDateTime now);

    int updateDocumentStatus(
            @Param("drftDocSn") Long drftDocSn,
            @Param("aprvlDocSttsCd") String aprvlDocSttsCd,
            @Param("aprvlCmptnDt") LocalDateTime aprvlCmptnDt,
            @Param("rtrnDt") LocalDateTime rtrnDt);

    List<ApprovalDraftSummaryResponse> selectMyDrafts(
            @Param("empId") Long empId,
            @Param("documentStatus") String documentStatus,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("size") int size);

    long countMyDrafts(
            @Param("empId") Long empId,
            @Param("documentStatus") String documentStatus,
            @Param("keyword") String keyword);

    List<ApprovalDraftSummaryResponse> selectMyApprovalRequests(
            @Param("empId") Long empId,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("size") int size);

    long countMyApprovalRequests(
            @Param("empId") Long empId,
            @Param("keyword") String keyword);

    List<ApprovalDraftSummaryResponse> selectMyApprovalHistory(
            @Param("empId") Long empId,
            @Param("keyword") String keyword,
            @Param("documentStatus") String documentStatus,
            @Param("offset") int offset,
            @Param("size") int size);

    long countMyApprovalHistory(
            @Param("empId") Long empId,
            @Param("keyword") String keyword,
            @Param("documentStatus") String documentStatus);

    List<ApprovalDraftSummaryResponse> selectMyCompletedApprovalDocuments(
            @Param("empId") Long empId,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("size") int size);

    long countMyCompletedApprovalDocuments(
            @Param("empId") Long empId,
            @Param("keyword") String keyword);

    ApprovalDocumentDetailResponse selectApprovalDocumentDetail(@Param("drftDocSn") Long drftDocSn);

    List<ApprovalStepStatusResponse> selectApprovalStepStatuses(@Param("drftDocSn") Long drftDocSn);

    int existsApprovalParticipant(
            @Param("drftDocSn") Long drftDocSn,
            @Param("empId") Long empId);

    ApprovalTemplateResponse selectUsableTemplate(
            @Param("empId") Long empId,
            @Param("tmplatCd") String tmplatCd);

    int existsUsableTemplate(@Param("tmplatCd") String tmplatCd);

    int existsTemplateFavorite(
            @Param("empId") Long empId,
            @Param("tmplatCd") String tmplatCd);

    void insertTemplateFavorite(
            @Param("empId") Long empId,
            @Param("tmplatCd") String tmplatCd,
            @Param("now") LocalDateTime now);

    int deleteTemplateFavorite(
            @Param("empId") Long empId,
            @Param("tmplatCd") String tmplatCd);

    void insertApprovalTemplate(ApprovalTemplateVO template);

    List<ApprovalTemplateResponse> selectAllUsableTemplates(@Param("empId") Long empId);
    
    Long selectTemplateOwnerByTemplateCode(@Param("tmplatCd") String tmplatCd);
    
    void updateTemplate(ApprovalTemplateVO object);

    /** 결재 양식 소프트 삭제 (USE_YN = 'N') */
    int softDeleteTemplate(
            @Param("tmplatCd") String tmplatCd,
            @Param("lastMdfrId") Long lastMdfrId,
            @Param("now") LocalDateTime now);

    /** 결재자(사원)의 전자서명 파일 ID 조회 (없으면 null) */
    Long selectEmpStampFileId(@Param("empId") Long empId);
    List<ApprovalDeadlineVO> selectApprovalsDueSoon();
}
