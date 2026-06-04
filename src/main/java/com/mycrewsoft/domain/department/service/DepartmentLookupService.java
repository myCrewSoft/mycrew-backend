package com.mycrewsoft.domain.department.service;

import com.mycrewsoft.domain.department.dto.response.DepartmentLookupResponse;

import java.util.List;

public interface DepartmentLookupService {

    List<DepartmentLookupResponse> lookupDepartments();
}