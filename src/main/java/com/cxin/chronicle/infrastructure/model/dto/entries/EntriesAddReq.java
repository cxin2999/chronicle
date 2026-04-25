package com.cxin.chronicle.infrastructure.model.dto.entries;

import com.cxin.chronicle.infrastructure.annotation.ValidEntryType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntriesAddReq {
    @NotBlank(message = "内容不能为空")
    private String content;
    @ValidEntryType(message = "记录类型无效，可选值：Done, Todo, Idea, Think, Rule")
    private String entryType;
}

