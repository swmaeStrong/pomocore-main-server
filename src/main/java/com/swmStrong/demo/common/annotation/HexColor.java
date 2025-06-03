package com.swmStrong.demo.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Pattern(
        regexp = "^#([A-F0-9]{6})$",
        message = "색상값은 #000000 - #FFFFFF 형식이어야 합니다."
)
public @interface HexColor {
    String message() default "색상값은 #000000 - #FFFFFF 형식이어야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
