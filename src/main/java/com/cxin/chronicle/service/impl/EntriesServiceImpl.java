package com.cxin.chronicle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxin.chronicle.infrastructure.convert.EntriesConvert;
import com.cxin.chronicle.infrastructure.model.dto.entries.*;
import com.cxin.chronicle.infrastructure.model.entity.Entries;
import com.cxin.chronicle.infrastructure.model.entity.User;
import com.cxin.chronicle.infrastructure.model.vo.EntriesVo;
import com.cxin.chronicle.infrastructure.model.vo.HeatmapDataVo;
import com.cxin.chronicle.mapper.EntriesMapper;
import com.cxin.chronicle.service.EntriesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 记录表 服务实现类
 * </p>
 *
 * @author Charles Chen
 * @since 2026-04-25
 */
@Service
public class EntriesServiceImpl extends ServiceImpl<EntriesMapper, Entries> implements EntriesService {

    @Resource
    private EntriesConvert entriesConvert;

    @Override
    public boolean addEntries(User loginUser, EntriesAddReq request) {
        Entries entries = new Entries();
        entries.setEntryType(request.getEntryType());
        entries.setContent(request.getContent());
        entries.setUserId(loginUser.getId());
        return this.save(entries);
    }

    @Override
    public List<EntriesVo> queryDailyEntries(User loginUser, EntriesQueryReq request) {
        // 解析日期字符串 yyyy-MM-dd
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(request.getDate(), formatter);

        // 计算当天的开始和结束时间
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // 构建查询条件：当前用户 + 指定日期范围
        LambdaQueryWrapper<Entries> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entries::getUserId, loginUser.getId())
                .ge(Entries::getCreateTime, startOfDay)
                .lt(Entries::getCreateTime, endOfDay)
                .orderByDesc(Entries::getCreateTime);

        List<Entries> entriesList = this.list(queryWrapper);
        List<EntriesVo> entriesVOList = entriesConvert.toEntryVoList(entriesList);
        return entriesVOList;
    }

    @Override
    public List<HeatmapDataVo> queryHeatmapData(User loginUser, int year) {
        return baseMapper.queryHeatmapData(loginUser.getId(), year);
    }

    @Override
    public boolean updateChecked(User loginUser, EntriesUpdateCheckedReq request) {
        // 构建更新条件：当前用户 + 指定ID
        LambdaQueryWrapper<Entries> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entries::getId, request.getId())
                .eq(Entries::getUserId, loginUser.getId());

        // 执行更新
        Entries entries = new Entries();
        entries.setChecked(request.getChecked());
        return this.update(entries, queryWrapper);
    }

    @Override
    public boolean updateContentAndType(User loginUser, EntriesUpdateContentAndTypeReq request) {
        // 构建更新条件：当前用户 + 指定ID
        LambdaQueryWrapper<Entries> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entries::getId, request.getId())
                .eq(Entries::getUserId, loginUser.getId());

        // 执行更新
        Entries entries = new Entries();
        entries.setContent(request.getContent());
        entries.setEntryType(request.getEntryType());
        return this.update(entries, queryWrapper);
    }

    @Override
    public boolean deleteEntry(User loginUser, EntriesDeleteReq request) {
        // 构建删除条件：当前用户 + 指定ID
        LambdaQueryWrapper<Entries> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entries::getId, request.getId())
                .eq(Entries::getUserId, loginUser.getId());

        // 执行逻辑删除（由于实体类使用了@TableLogic注解）
        return this.remove(queryWrapper);
    }

    @Override
    public List<EntriesVo> queryHistoryWithCursor(User loginUser, EntriesHistoryReq request) {
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        List<Entries> entriesList = baseMapper.queryHistoryWithCursor(
                loginUser.getId(),
                request.getLastCreateTime(),
                pageSize
        );
        return entriesConvert.toEntryVoList(entriesList);
    }
}
