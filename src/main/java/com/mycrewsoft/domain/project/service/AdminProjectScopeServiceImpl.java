package com.mycrewsoft.domain.project.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.project.dto.AdminProjectListResponseDto;
import com.mycrewsoft.domain.project.mapper.AdminProjectScopeMapper;
import com.mycrewsoft.domain.project.vo.ProjectVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProjectScopeServiceImpl implements AdminProjectScopeService {

    private final AuthorizationService authorizationService;
    private final AdminProjectScopeMapper adminProjectScopeMapper;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AdminScopeOptionResponseDTO> getProjectScopeOptions() {
        assertRoleManagePermission();
        return adminProjectScopeMapper.selectProjectScopeOptions();
    }

    private void assertRoleManagePermission() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.ADMIN)
                .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.ADMIN_ROLE_MANAGE, resource);
    }

	/**
	 * 프로젝트(관리자) 전체 목록 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AdminProjectListResponseDto> getAdminProjectList() {
		assertRoleManagePermission();
		List<ProjectVO> vo = adminProjectScopeMapper.selectAdminProjectList();
		return dtoMapper.toDtoList(vo, AdminProjectListResponseDto.class);
	}
}
