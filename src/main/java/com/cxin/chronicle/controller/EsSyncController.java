package com.cxin.chronicle.controller;

import com.cxin.chronicle.infrastructure.common.BaseResponse;
import com.cxin.chronicle.infrastructure.common.ResultUtils;
import com.cxin.chronicle.infrastructure.utils.EsSyncUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Elasticsearch 数据同步控制器
 *
 * @author Charles Chen
 * @since 2026-05-03
 */
@Slf4j
@RestController
@RequestMapping("/es")
@Tag(name = "Elasticsearch管理", description = "ES数据同步相关接口")
public class EsSyncController {

    @Resource
    private EsSyncUtil esSyncUtil;

    /**
     * 全量同步数据到 ES（逐条插入）
     */
    @PostMapping("/sync/all")
    @Operation(hidden = true, summary = "全量同步数据到ES", description = "将MySQL中所有未删除的记录同步到Elasticsearch")
    public BaseResponse<Integer> syncAll() {
        log.info("收到全量同步请求");
        int count = esSyncUtil.syncAllToEs();
        return ResultUtils.success(count);
    }

    /**
     * 批量同步数据到 ES（使用 Bulk API，推荐）
     */
    @PostMapping("/sync/bulk")
    @Operation(hidden = true,summary = "批量同步数据到ES", description = "使用Bulk API高效批量同步数据")
    public BaseResponse<Integer> bulkSync() {
        log.info("收到批量同步请求");
        int count = esSyncUtil.bulkSyncToEs();
        return ResultUtils.success(count);
    }
}
