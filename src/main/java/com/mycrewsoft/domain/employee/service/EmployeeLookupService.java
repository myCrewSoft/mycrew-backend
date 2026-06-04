package com.mycrewsoft.domain.employee.service;

import com.mycrewsoft.domain.employee.dto.request.EmployeeLookupRequest;
import com.mycrewsoft.domain.employee.dto.response.EmployeeLookupResponse;

import java.util.List;

public interface EmployeeLookupService {

    List<EmployeeLookupResponse> lookupEmployees(EmployeeLookupRequest request);
}