package com.cxin.chronicle.infrastructure.valid;


import com.cxin.chronicle.infrastructure.annotation.ValidEntryType;
import com.cxin.chronicle.infrastructure.model.enums.EntryTypeEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidEntryTypeValidator implements ConstraintValidator<ValidEntryType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return EntryTypeEnum.getEnumByValue(value) != null;
    }
}
