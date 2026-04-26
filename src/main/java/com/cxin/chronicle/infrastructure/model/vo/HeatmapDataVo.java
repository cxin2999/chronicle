package com.cxin.chronicle.infrastructure.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeatmapDataVo {
    @Schema(description = "日期（格式：yyyy-MM-dd）")
    private String date;

    @Schema(description = "记录数量")
    private Long count;
}
