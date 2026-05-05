package com.cxin.chronicle.infrastructure.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应VO
 *
 * @author Charles Chen
 * @since 2026-05-03
 */
@Data
@Schema(description = "分页响应")
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> records;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private Long total;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private Integer pageSize;

    /**
     * 总页数
     */
    @Schema(description = "总页数")
    private Integer pages;

    public PageResponse() {
    }

    public PageResponse(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }
}
