package com.mycrewsoft.domain.schedule.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.schedule.dto.request.AdminSchdRequest;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdListResponse;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdResponse;
import com.mycrewsoft.domain.schedule.mapper.AdminSchdDtoMapper;
import com.mycrewsoft.domain.schedule.mapper.AdminSchdMapper;
import com.mycrewsoft.domain.schedule.mapper.SchdTargetMapper;
import com.mycrewsoft.domain.schedule.vo.AdminSchdListVO;
import com.mycrewsoft.domain.schedule.vo.AdminSchdSearchVO;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetDetailVO;
import com.mycrewsoft.domain.schedule.vo.SchdTargetVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminScheduleServiceImpl implements AdminScheduleService {

    private final AdminSchdMapper adminSchdMapper;
    private final SchdTargetMapper schdTargetMapper;
    private final AdminSchdDtoMapper adminSchdDtoMapper;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminSchdListResponse> getSchdList(AdminSchdSearchVO search, Pageable pageable) {
        assertPermission();

        long total = adminSchdMapper.countAdminSchdList(search);
        List<AdminSchdListVO> content = adminSchdMapper.selectAdminSchdList(
            search,
            pageable.getOffset(),
            pageable.getPageSize()
        );

        List<AdminSchdListResponse> dtoList = adminSchdDtoMapper.toListResponseList(content);

        return new PageImpl<>(dtoList, pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSchdResponse getSchd(Long schdId) {
        assertPermission();

        IntgSchdVO vo = adminSchdMapper.selectAdminSchd(schdId);
        if (vo == null) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);

        List<SchdTargetDetailVO> targets = schdTargetMapper.selectSchdTargetDetail(schdId);

        return adminSchdDtoMapper.toResponse(vo, targets);
    }

    @Override
    @Transactional
    public Long createSchd(AdminSchdRequest dto) {
        assertPermission();

        Long empId = SecurityUtil.getCurrentEmpId();

        IntgSchdVO vo = adminSchdDtoMapper.toVO(dto, empId);
        adminSchdMapper.insertAdminSchd(vo);

        List<SchdTargetVO> targets = buildAdminTargetList(dto, vo.getSchdId());
        if (!targets.isEmpty()) {
            schdTargetMapper.insertSchdTargetList(targets);
        }

        return vo.getSchdId();
    }

    @Override
    @Transactional
    public void modifySchd(Long schdId, AdminSchdRequest dto) {
        assertPermission();

        IntgSchdVO existing = adminSchdMapper.selectAdminSchd(schdId);
        if (existing == null) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);

        Long empId = SecurityUtil.getCurrentEmpId();

        IntgSchdVO updateVO = adminSchdDtoMapper.toVO(dto, empId);
        updateVO.setSchdId(schdId);
        updateVO.setSchdChgrId(empId);

        adminSchdMapper.updateAdminSchd(updateVO);

        schdTargetMapper.deleteSchdTarget(schdId);
        List<SchdTargetVO> targets = buildAdminTargetList(dto, schdId);
        if (!targets.isEmpty()) {
            schdTargetMapper.insertSchdTargetList(targets);
        }
    }

    @Override
    @Transactional
    public void deleteSchd(Long schdId) {
        assertPermission();

        IntgSchdVO existing = adminSchdMapper.selectAdminSchd(schdId);
        if (existing == null) throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);

        schdTargetMapper.deleteSchdTarget(schdId);
        adminSchdMapper.deleteAdminSchd(schdId);
    }

    private void assertPermission() {
        ResourceContext resource = ResourceContext.builder()
            .resourceType(ResourceType.SCHEDULE)
            .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.SCHEDULE_MANAGE, resource);
    }
    
    private List<SchdTargetVO> buildAdminTargetList(AdminSchdRequest dto, Long schdId) {
        List<SchdTargetVO> targets = new ArrayList<>();

        if ("C001".equals(dto.getSchdClsfCd())) {
            // 전사 일정
            targets.add(SchdTargetVO.builder()
                    .schdId(schdId).targetTypeCd("01").targetId("0").build());

        } else if ("C003".equals(dto.getSchdClsfCd())) {
            // 간부 일정
            targets.add(SchdTargetVO.builder()
                    .schdId(schdId).targetTypeCd("03").targetId("0").build());

        } else if ("C004".equals(dto.getSchdClsfCd())) {
            // 부서 일정
            targets.add(SchdTargetVO.builder()
                    .schdId(schdId).targetTypeCd("04").targetId(dto.getDeptCd()).build());
        }

        return targets;
    }
}