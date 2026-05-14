package com.mycrewsoft.app.validate.validator;

import com.mycrewsoft.app.validate.constraints.StrongPassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @StrongPassword 어노테이션의 실제 검증 로직을 수행하는 클래스.
 * ConstraintValidator<어노테이션 타입, 검증할 필드 타입> 을 구현한다.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private String regex;

    /**
     * min, max 속성값으로 정규식을 동적으로 생성한다.
     * 정규식: 영문자 1개 이상 + 숫자 1개 이상 + 특수문자(@!#) 1개 이상 포함
     */
    @Override
    public void initialize(StrongPassword annotation) {
        this.regex = String.format(
                "^(?=.*[a-zA-Z])(?=.*[\\d])(?=.*[@!#])[\\w@!#]{%d,%d}$", annotation.min(), annotation.max());
    }

    /**
     * 실제 검증 로직을 수행한다.
     * null 또는 blank 값은 통과시킨다. 필수 여부는 @NotBlank 가 담당한다.
     * 정규식과 일치하면 true, 불일치하면 false 를 반환한다.
     *
     * @param value   검증할 비밀번호 값
     * @param context 검증 컨텍스트
     * @return 유효하면 true, 아니면 false
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        
        return value.matches(regex);
    }
}