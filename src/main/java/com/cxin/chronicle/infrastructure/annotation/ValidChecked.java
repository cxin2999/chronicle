package com.cxin.chronicle.infrastructure.annotation;

import com.cxin.chronicle.infrastructure.valid.ValidCheckedValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {ValidCheckedValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidChecked {
    String message() default "勾选状态只能为0或1";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
