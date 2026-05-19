package com.mycrewsoft.validate.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mycrewsoft.validate.validator.PhoneNumberValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 전화번호 형식을 검증하는 커스텀 어노테이션.
 * 사용 예:
 * 
 * @PhoneNumber → 일반 전화번호
 * @PhoneNumber(regex = "010-\\d{3,4}-\\d{4}") → 휴대폰 전용
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface PhoneNumber {
    /** 검증에 사용할 정규식. 기본값은 일반 전화번호 형식 */
    String regex() default "\\d{2,3}-\\d{3,4}-\\d{4}";

    /** 오류 메시지에 표시할 형식 예시 */
    String displayPtrn() default "02-000-0000 또는 042-000-0000";

    /** 오류 메시지. ${validatedValue} 는 실제 입력값, {displayPtrn} 은 위 속성값으로 치환됨 */
    String message() default "입력값(${validatedValue}), 전화번호 형식 확인 → {displayPtrn}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
