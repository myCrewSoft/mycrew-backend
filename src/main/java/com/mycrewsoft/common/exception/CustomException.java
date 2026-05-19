package com.mycrewsoft.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 의도적으로 발생시키는 커스텀 예외.
 * ErrorCode 를 담아서 throw 하면 GlobalExceptionHandler 가 자동으로 처리한다.
 * Service 레이어에서만 throw. Controller 에서 try-catch 금지.
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}