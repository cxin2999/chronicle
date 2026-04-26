package com.cxin.chronicle.controller;

import com.cxin.chronicle.infrastructure.annotation.AuthCheck;
import com.cxin.chronicle.infrastructure.common.BaseResponse;
import com.cxin.chronicle.infrastructure.common.ResultUtils;
import com.cxin.chronicle.infrastructure.constant.UserConstant;
import com.cxin.chronicle.infrastructure.model.dto.entries.*;
import com.cxin.chronicle.infrastructure.model.entity.User;
import com.cxin.chronicle.infrastructure.model.vo.EntriesVo;
import com.cxin.chronicle.infrastructure.model.vo.HeatmapDataVo;
import com.cxin.chronicle.service.EntriesService;
import com.cxin.chronicle.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 记录表 前端控制器
 * </p>
 *
 * @author Charles Chen
 * @since 2026-04-25
 */
@RestController
@RequestMapping("/entries")
public class EntriesController {

    @Resource
    private EntriesService entriesService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    @Operation(summary = "添加记录")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> createArticle(@RequestBody @Valid EntriesAddReq request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getCurrentUser(httpServletRequest);
        Boolean added = entriesService.addEntries(loginUser, request);
        return ResultUtils.success(added);
    }

    @PostMapping("/query/daily")
    @Operation(summary = "查询日记录")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<EntriesVo>> queryDailyEntries(@RequestBody @Valid EntriesQueryReq request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getCurrentUser(httpServletRequest);
        List<EntriesVo> entriesVoList = entriesService.queryDailyEntries(loginUser, request);
        return ResultUtils.success(entriesVoList);
    }

    @PostMapping("/query/heatmap")
    @Operation(summary = "查询热力图数据")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<HeatmapDataVo>> queryHeatmapData(
            @RequestParam String year,
            HttpServletRequest httpServletRequest) {
        User loginUser = userService.getCurrentUser(httpServletRequest);
        // 解析年份参数，格式：yyyy
        int yearInt = Integer.parseInt(year);
        List<HeatmapDataVo> heatmapDataList = entriesService.queryHeatmapData(loginUser, yearInt);
        return ResultUtils.success(heatmapDataList);
    }

    @PostMapping("/update/checked")
    @Operation(summary = "更新记录勾选状态")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> updateChecked(@RequestBody @Valid EntriesUpdateCheckedReq request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getCurrentUser(httpServletRequest);
        Boolean updated = entriesService.updateChecked(loginUser, request);
        return ResultUtils.success(updated);
    }

    @PostMapping("/update/content-and-type")
    @Operation(summary = "更新记录内容和类型")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> updateContentAndType(@RequestBody @Valid EntriesUpdateContentAndTypeReq request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getCurrentUser(httpServletRequest);
        Boolean updated = entriesService.updateContentAndType(loginUser, request);
        return ResultUtils.success(updated);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除记录")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> deleteEntry(@RequestBody @Valid EntriesDeleteReq request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getCurrentUser(httpServletRequest);
        Boolean deleted = entriesService.deleteEntry(loginUser, request);
        return ResultUtils.success(deleted);
    }
}
