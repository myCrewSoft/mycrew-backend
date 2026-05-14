package com.mycrewsoft.app.validate.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

import com.mycrewsoft.app.validate.validator.StrongPasswordValidator;

/**
 * 비밀번호 강도를 검증하는 커스텀 어노테이션.
 * 영문자, 숫자, 특수문자(@!#) 를 모두 포함하고 길이 조건을 만족해야 한다.
 * min, max 속성으로 허용 길이를 조정할 수 있다.
 *
 * 사용 예:
 * 
 * @StrongPassword → 기본값: 8~20자
 * @StrongPassword(min = 10, max = 16) → 10~16자
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    int min() default 8;

    int max() default 20;

    /** 오류 메시지. {min}, {max} 는 위 속성값으로 치환됨 */
    String message() default "비밀번호는 {min}~{max}자이며, 영문·숫자·특수문자(@!#)를 모두 포함해야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}