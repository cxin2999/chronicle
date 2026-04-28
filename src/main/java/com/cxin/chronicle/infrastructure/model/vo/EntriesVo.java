package com.cxin.chronicle.infrastructure.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntriesVo {
    @Schema(description = "id")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "记录内容")
    private String content;

    @Schema(description = "记录类型 枚举值:Do/Idea/Think/Rule")
    private String entryType;

    @Schema(description = "是否勾选")
    private Integer checked;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
