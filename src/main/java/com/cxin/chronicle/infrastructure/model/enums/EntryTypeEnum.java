package com.cxin.chronicle.infrastructure.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum EntryTypeEnum {
    Done("Done", "完成"),
    Todo("Todo", "待办"),
    Idea("Idea", "想法"),
    Think("Think", "思考"),
    Rule("Rule", "原则");

    private final String description;

    private final String value;

    EntryTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static EntryTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (EntryTypeEnum anEnum : EntryTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
