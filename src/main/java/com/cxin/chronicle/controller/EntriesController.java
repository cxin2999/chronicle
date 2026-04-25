package com.cxin.chronicle.controller;

import com.cxin.chronicle.infrastructure.annotation.AuthCheck;
import com.cxin.chronicle.infrastructure.common.BaseResponse;
import com.cxin.chronicle.infrastructure.common.ResultUtils;
import com.cxin.chronicle.infrastructure.constant.UserConstant;
import com.cxin.chronicle.infrastructure.model.dto.entries.EntriesAddReq;
import com.cxin.chronicle.infrastructure.model.entity.User;
import com.cxin.chronicle.service.EntriesService;
import com.cxin.chronicle.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
