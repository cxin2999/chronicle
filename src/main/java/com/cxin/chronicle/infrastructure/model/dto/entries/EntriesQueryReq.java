package com.cxin.chronicle.infrastructure.model.dto.entries;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EntriesQueryReq {
    @Schema(description = "记录日期（格式：yyyy-MM-dd）")
    @NotBlank(message = "日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式不正确")
    private String date;
}
