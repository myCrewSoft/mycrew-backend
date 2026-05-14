package com.mycrewsoft.app.common.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.mycrewsoft.app.common.response.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 애플리케이션 전체에서 발생하는 모든 예외를 한 곳에서 처리한다.
 * 처리 순서: MaxUploadSizeExceededException → CustomException → @Valid 실패
 * → 타입 불일치 → JSON 파싱 실패
 * → HTTP 메서드 불일치 → 필수 파라미터 누락 → Exception(최후)
 * 로그 전략: 4xx → log.warn / 5xx → log.error
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * application.properties 의 spring.servlet.multipart.max-file-size 초과 시 발생.
     * FileUtil 의 MAX_IMAGE_SIZE / MAX_DOCUMENT_SIZE 보다 먼저 걸림.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(
            MaxUploadSizeExceededException e) {
        log.warn("[MaxUploadSize] {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("파일 크기가 초과되었습니다.",
                        ErrorCode.FILE_SIZE_EXCEEDED.getCode()));
    }

    /**
     * Service 에서 throw new CustomException(ErrorCode.XXX) 로 던진 예외를 처리한다.
     * HTTP 상태코드와 에러 코드는 ErrorCode 에 정의된 값을 그대로 사용한다.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode ec = e.getErrorCode();
        if (ec.getHttpStatus().is5xxServerError())
            log.error("[CustomException] code:{}, message:{}", ec.getCode(), e.getMessage());
        else
            log.warn("[CustomException] code:{}, message:{}", ec.getCode(), e.getMessage());
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ApiResponse.fail(e.getMessage(), ec.getCode()));
    }

    /**
     * @Validated 또는 @Valid 검증이 실패했을 때 처리한다.
     *            모든 필드 에러를 수집하고, 첫 번째 에러 메시지를 대표 메시지로 반환한다.
     *            data 필드에 필드별 에러 목록(FieldErrorDetail)을 함께 반환한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<FieldErrorDetail>>> handleValidationException(
            MethodArgumentNotValidException e) {
        BindingResult br = e.getBindingResult();
        List<FieldErrorDetail> fieldErrors = br.getFieldErrors().stream()
                .map(FieldErrorDetail::of).collect(Collectors.toList());
        String firstMsg = br.getFieldErrors().stream().findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());
        log.warn("[ValidationException] errors:{}", fieldErrors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(firstMsg, ErrorCode.INVALID_INPUT_VALUE.getCode(), fieldErrors));
    }

    /** PathVariable 타입 불일치. 예) /users/{id} 에 문자열이 들어온 경우. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        String msg = String.format("'%s' 파라미터의 타입이 올바르지 않습니다.", e.getName());
        log.warn("[TypeMismatch] {}", msg);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(msg, ErrorCode.INVALID_TYPE_VALUE.getCode()));
    }

    /** Request Body JSON 형식 오류. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(
            HttpMessageNotReadableException e) {
        log.warn("[NotReadable] {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("요청 본문의 형식이 올바르지 않습니다.",
                        ErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    /** 허용되지 않는 HTTP 메서드. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e) {
        log.warn("[MethodNotSupported] {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.METHOD_NOT_ALLOWED.getMessage(),
                        ErrorCode.METHOD_NOT_ALLOWED.getCode()));
    }

    /** 필수 @RequestParam 누락. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException e) {
        String msg = String.format("필수 파라미터 '%s' 가 누락되었습니다.", e.getParameterName());
        log.warn("[MissingParam] {}", msg);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(msg, ErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    /** 최후 방어선. 위에서 처리되지 않은 모든 예외. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[UnhandledException] {}", ExceptionUtils.getRootCauseMessage(e), e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }

    /**
     * 유효성 검사 실패 시 어떤 필드가 왜 실패했는지 담는 내부 클래스.
     * JSON 응답의 data 배열 각 항목으로 사용된다.
     */
    @Getter
    public static class FieldErrorDetail {
        private final String field; // 실패한 필드명
        private final String message; // 실패 이유 메시지

        private FieldErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public static FieldErrorDetail of(FieldError e) {
            return new FieldErrorDetail(e.getField(), e.getDefaultMessage());
        }
    }
}