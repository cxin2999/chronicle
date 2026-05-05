package com.cxin.chronicle.infrastructure.model.dto.entries;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntriesHistoryReq {

    @Schema(description = "每页大小，默认为 10")
    private Integer pageSize = 10;

    @Schema(description = "上一页最后一条记录的创建时间（首次请求不传）")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastCreateTime;
}
