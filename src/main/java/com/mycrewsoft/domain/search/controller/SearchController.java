package com.mycrewsoft.domain.search.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.search.dto.response.SearchResponse;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.service.SearchService;
import com.mycrewsoft.domain.search.vo.RecentItemVO;
import com.mycrewsoft.domain.search.vo.SearchHistVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "통합 검색")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "통합 검색")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) SearchType type
    ) {
        return ResponseEntity.ok(ApiResponse.success(searchService.search(keyword, type)));
    }

    @Operation(summary = "검색 이력 조회")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SearchHistVO>>> readSearchHist() {
        return ResponseEntity.ok(ApiResponse.success(searchService.readSearchHist()));
    }

    @Operation(summary = "검색 이력 단건 삭제")
    @DeleteMapping("/history/{searchHistId}")
    public ResponseEntity<ApiResponse<Void>> deleteSearchHist(@PathVariable Long searchHistId) {
        searchService.deleteSearchHist(searchHistId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "검색 이력 전체 삭제")
    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<Void>> deleteAllSearchHist() {
        searchService.deleteAllSearchHist();
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "최근 본 항목 등록")
    @PostMapping("/recent")
    public ResponseEntity<ApiResponse<Void>> mergeRecentItem(
            @RequestParam SearchType itemType,
            @RequestParam Long itemId
    ) {
        searchService.mergeRecentItem(itemType, itemId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "최근 본 항목 조회")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<RecentItemVO>>> readRecentItems() {
        return ResponseEntity.ok(ApiResponse.success(searchService.readRecentItems()));
    }

    @Operation(summary = "최근 본 항목 단건 삭제")
    @DeleteMapping("/recent/{recentItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteRecentItem(@PathVariable Long recentItemId) {
        searchService.deleteRecentItem(recentItemId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}