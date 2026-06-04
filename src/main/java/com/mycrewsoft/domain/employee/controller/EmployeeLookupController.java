package com.mycrewsoft.domain.employee.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.request.EmployeeLookupRequest;
import com.mycrewsoft.domain.employee.dto.response.EmployeeLookupResponse;
import com.mycrewsoft.domain.employee.service.EmployeeLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "사원 조회", description = "공통 사원 검색 컴포넌트용 API")
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeLookupController {

    private final EmployeeLookupService employeeLookupService;

    @Operation(summary = "공통 사원 검색", description = "사원명·부서명·직급명 통합 검색 및 부서 필터링")
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<List<EmployeeLookupResponse>>> lookupEmployees(
            @ModelAttribute EmployeeLookupRequest request) {

        List<EmployeeLookupResponse> result = employeeLookupService.lookupEmployees(request);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}