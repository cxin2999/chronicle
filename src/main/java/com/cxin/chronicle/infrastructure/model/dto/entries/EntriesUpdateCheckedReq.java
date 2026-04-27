package com.cxin.chronicle.infrastructure.model.dto.entries;

import com.cxin.chronicle.infrastructure.annotation.ValidChecked;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新记录勾选状态请求
 *
 * @author Charles Chen
 * @since 2026-04-26
 */
@Data
public class EntriesUpdateCheckedReq {

    /**
     * 记录ID
     */
    @NotNull(message = "记录ID不能为空")
    @Schema(description = "记录ID")
    private String id;

    /**
     * 是否勾选 0-未勾选 1-已勾选
     */
    @ValidChecked(message = "勾选状态只能为0或1")
    @Schema(description = "是否勾选 0-未勾选 1-已勾选")
    private Integer checked;
}
