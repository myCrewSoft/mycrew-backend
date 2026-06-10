package com.mycrewsoft.domain.approval.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.approval.approvaltemplateVO.ApprovalTemplateVO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalTemplateCreateRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalUpdateRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalMutationResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApprovalTemplateServiceImpl implements ApprovalTemplateService {

    private final ApprovalDraftMapper approvalDraftMapper;
    private final ApprovalServiceSupport serviceSupport;
    private final DtoMapper dtoMapper;
    
    @Transactional
    @Override
    public ApprovalTemplateResponse createTemplate(ApprovalTemplateCreateRequestDTO requestDTO) {
    	serviceSupport.assertTemplateCreatePermission();
    	
        Long empId = SecurityUtil.getCurrentEmpId();
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 템플릿 코드 자동 생성
        String tmplatCd = generateTemplateCode();
        
        // 2. VO 객체 생성 및 저장
        ApprovalTemplateVO templateVO = dtoMapper.toDto(requestDTO, ApprovalTemplateVO.class);
        templateVO.setTmplatCd(tmplatCd);
        templateVO.setUseYn("Y");
        templateVO.setFrstRgtrId(empId);
        templateVO.setFrstRegDt(now);
        templateVO.setLastMdfrId(empId);
        templateVO.setLastMdfcnDt(now);
        
        // 3. 데이터베이스에 저장
        approvalDraftMapper.insertApprovalTemplate(templateVO);
        
        // 4. Response DTO 반환
        return approvalDraftMapper.selectUsableTemplate(empId, tmplatCd);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ApprovalTemplateResponse> loadTemplates() {
        Long empId = SecurityUtil.getCurrentEmpId();
        return approvalDraftMapper.selectAllUsableTemplates(empId);
    }

    @Transactional(readOnly = true)
    @Override
    public ApprovalTemplateResponse loadTemplate(String tmplatCd) {
        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalTemplateResponse template = approvalDraftMapper.selectUsableTemplate(empId, tmplatCd);
        if (template == null) {
            throw new CustomException(com.mycrewsoft.common.exception.ErrorCode.INVALID_INPUT_VALUE);
        }
        return template;
    }

    @Transactional
    @Override
    public ApprovalMutationResponse toggleTemplateFavorite(String tmplatCd) {
        Long empId = SecurityUtil.getCurrentEmpId();
        if (approvalDraftMapper.existsUsableTemplate(tmplatCd) == 0) {
            throw new CustomException(com.mycrewsoft.common.exception.ErrorCode.INVALID_INPUT_VALUE);
        }
        boolean exists = approvalDraftMapper.existsTemplateFavorite(empId, tmplatCd) > 0;
        if (exists) {
            approvalDraftMapper.deleteTemplateFavorite(empId, tmplatCd);
            return new ApprovalMutationResponse(null, null, "템플릿 즐겨찾기를 해제했습니다.");
        }
        approvalDraftMapper.insertTemplateFavorite(empId, tmplatCd, LocalDateTime.now());
        return new ApprovalMutationResponse(null, null, "템플릿 즐겨찾기를 등록했습니다.");
    }

    /**
     * 템플릿 코드 자동 생성
     * 형식: TMPL_<8자리UUID>
     */
    private String generateTemplateCode() {
        return "TMPL_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

	@Override
	@Transactional
	public void updateTemplates(ApprovalUpdateRequestDTO request) {
		String tmplateCode = request.getTmplatCd();
		Long ownerEmpId = approvalDraftMapper.selectTemplateOwnerByTemplateCode(tmplateCode);
		
		if(ownerEmpId == 0) {
			throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND);
		}
		
		serviceSupport.assertTemplateUpdatePermission(ownerEmpId);
		ApprovalTemplateVO newOne = dtoMapper.toDto(request, ApprovalTemplateVO.class);
		newOne.setLastMdfcnDt(LocalDateTime.now());
		newOne.setLastMdfrId(SecurityUtil.getCurrentEmpId());
		newOne.setUseYn("Y");
		
		approvalDraftMapper.updateTemplate(newOne);
	}
}
