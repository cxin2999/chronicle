package com.cxin.chronicle.infrastructure.annotation;


import com.cxin.chronicle.infrastructure.valid.ValidEntryTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {ValidEntryTypeValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEntryType {
    String message() default "无效的记录类型";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
