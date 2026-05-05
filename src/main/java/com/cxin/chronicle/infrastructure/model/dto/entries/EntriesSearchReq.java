package com.cxin.chronicle.infrastructure.model.dto.entries;

import com.cxin.chronicle.infrastructure.annotation.ValidEntryType;
import com.cxin.chronicle.infrastructure.common.PageRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntriesSearchReq extends PageRequest {

    /**
     * 关键字
     */
    @Schema(description = "关键字")
    private String keyword;


    /**
     * 记录类型 枚举值:Do/Idea/Think/Rule
     */
    @ValidEntryType
    @Schema(description = "记录类型 枚举值:Do/Idea/Think/Rule")
    private String entryType;
}
