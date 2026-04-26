package com.cxin.chronicle.infrastructure.model.dto.entries;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除记录请求
 *
 * @author Charles Chen
 * @since 2026-04-26
 */
@Data
public class EntriesDeleteReq {

    /**
     * 记录ID
     */
    @NotNull(message = "记录ID不能为空")
    @Schema(description = "记录ID")
    private String id;
}
