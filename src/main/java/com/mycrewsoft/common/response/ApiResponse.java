package com.mycrewsoft.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * 모든 API 응답의 표준 래퍼 클래스.
 * null 인 필드는 @JsonInclude 설정으로 JSON 직렬화에서 자동 제외된다.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;   // 요청 성공 여부
    private final String message;    // 사람이 읽을 수 있는 메시지
    private final T data;            // 실제 응답 데이터 (실패 시 null → JSON 제외)
    private final String errorCode;  // 실패 시 에러 코드 (성공 시 null → JSON 제외)
    private final PageInfo pagination; // 목록 조회 시 페이지 정보 (없을 때 null → JSON 제외)

    private ApiResponse(boolean success, String message, T data,
                        String errorCode, PageInfo pagination) {
        this.success    = success;
        this.message    = message;
        this.data       = data;
        this.errorCode  = errorCode;
        this.pagination = pagination;
    }

    /** 데이터가 있는 성공 응답. 단건 조회, 수정 결과 반환 등에 사용한다. */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청이 성공했습니다.", data, null, null);
    }

    /** 메시지를 커스텀하고 데이터가 있는 성공 응답. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    /** 데이터가 없는 성공 응답. 삭제 완료, 수정 완료 등에 사용한다. */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, "요청이 성공했습니다.", null, null, null);
    }

    /** 페이지네이션 정보가 포함된 목록 성공 응답. */
    public static <T> ApiResponse<List<T>> success(List<T> content, Page<?> page) {
        return new ApiResponse<>(true, "요청이 성공했습니다.", content, null, PageInfo.of(page));
    }

    /** 실패 응답. GlobalExceptionHandler 에서만 호출한다. */
    public static <T> ApiResponse<T> fail(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode, null);
    }

    /** 필드별 에러 목록 포함 실패 응답. @Valid 검증 실패 시 사용한다. */
    public static <T> ApiResponse<T> fail(String message, String errorCode, T data) {
        return new ApiResponse<>(false, message, data, errorCode, null);
    }
    
    /**
     * 페이지네이션 정보를 담는 내부 클래스.
     * 목록 조회 응답에만 포함되며, 없을 때는 JSON 에서 자동 제외된다.
     */
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PageInfo {
        private final int page;            // 현재 페이지 번호 (0-based)
        private final int size;            // 페이지당 항목 수
        private final long totalElements; // 전체 항목 수
        private final int totalPages;     // 전체 페이지 수
        private final boolean first;      // 첫 페이지 여부
        private final boolean last;       // 마지막 페이지 여부

        private PageInfo(Page<?> page) {
            this.page          = page.getNumber();
            this.size          = page.getSize();
            this.totalElements = page.getTotalElements();
            this.totalPages    = page.getTotalPages();
            this.first         = page.isFirst();
            this.last          = page.isLast();
        }
        
		/** Spring Data Page 객체로부터 PageInfo 를 생성한다. */
        public static PageInfo of(Page<?> page) { return new PageInfo(page); }
    }
}