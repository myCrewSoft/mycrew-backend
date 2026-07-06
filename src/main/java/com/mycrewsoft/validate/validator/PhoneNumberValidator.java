package com.mycrewsoft.validate.validator;

import com.mycrewsoft.validate.constraints.PhoneNumber;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @PhoneNumber 어노테이션의 실제 검증 로직을 수행하는 클래스.
 * ConstraintValidator<어노테이션 타입, 검증할 필드 타입> 을 구현한다.
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private String regex;

    /** 어노테이션에 설정된 regex 속성값을 꺼내 저장한다. */
    @Override
    public void initialize(PhoneNumber annotation) {
        this.regex = annotation.regex();
    }

    /**
     * 실제 검증 로직을 수행한다.
     * null 또는 blank 값은 통과시킨다. 필수 여부는 @NotBlank 가 담당한다.
     * 정규식과 일치하면 true, 불일치하면 false 를 반환한다.
     *
     * @param value   검증할 필드 값
     * @param context 검증 컨텍스트 (커스텀 메시지 변경 등에 사용)
     * @return 유효하면 true, 아니면 false
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        
        return value.matches(regex);
    }
}