package com.cxin.chronicle.infrastructure.valid;

import com.cxin.chronicle.infrastructure.annotation.ValidChecked;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCheckedValidator implements ConstraintValidator<ValidChecked, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // null 值由 @NotNull 注解处理，这里只验证非空值
        if (value == null) {
            return true;
        }
        // 只能为 0 或 1
        return value == 0 || value == 1;
    }
}
