package com.cxin.chronicle.infrastructure.model.dto.entries;

import com.cxin.chronicle.infrastructure.annotation.ValidEntryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新记录内容和类型请求
 *
 * @author Charles Chen
 * @since 2026-04-26
 */
@Data
public class EntriesUpdateContentAndTypeReq {

    /**
     * 记录ID
     */
    @NotNull(message = "记录ID不能为空")
    @Schema(description = "记录ID")
    private String id;

    /**
     * 记录内容
     */
    @NotBlank(message = "记录内容不能为空")
    @Schema(description = "记录内容")
    private String content;

    /**
     * 记录类型 枚举值:Do/Idea/Think/Rule
     */
    @ValidEntryType
    @NotBlank(message = "记录类型不能为空")
    @Schema(description = "记录类型 枚举值:Do/Idea/Think/Rule")
    private String entryType;
}
