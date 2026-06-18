package com.mycrewsoft.domain.schedule.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycrewsoft.domain.schedule.dto.request.AdminSchdRequest;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdListResponse;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdResponse;
import com.mycrewsoft.domain.schedule.vo.AdminSchdSearchVO;

public interface AdminScheduleService {

    Page<AdminSchdListResponse> getSchdList(AdminSchdSearchVO search, Pageable pageable);

    AdminSchdResponse getSchd(Long schdId);

    Long createSchd(AdminSchdRequest dto);

    void modifySchd(Long schdId, AdminSchdRequest dto);

    void deleteSchd(Long schdId);
}