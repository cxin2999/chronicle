package com.cxin.chronicle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxin.chronicle.infrastructure.convert.EntriesConvert;
import com.cxin.chronicle.infrastructure.model.document.EntriesDocument;
import com.cxin.chronicle.infrastructure.model.dto.entries.*;
import com.cxin.chronicle.infrastructure.model.entity.Entries;
import com.cxin.chronicle.infrastructure.model.entity.User;
import com.cxin.chronicle.infrastructure.model.vo.EntriesVo;
import com.cxin.chronicle.infrastructure.model.vo.HeatmapDataVo;
import com.cxin.chronicle.mapper.EntriesMapper;
import com.cxin.chronicle.service.EntriesEsSearchService;
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

    @Resource
    private EntriesEsSearchService entriesEsSearchService;

    @Override
    public boolean addEntries(User loginUser, EntriesAddReq request) {
        Entries entries = new Entries();
        entries.setEntryType(request.getEntryType());
        entries.setContent(request.getContent());
        entries.setUserId(loginUser.getId());
        boolean result = this.save(entries);
        
        // 双写：同步到 ES
        if (result) {
            try {
                EntriesDocument document = entriesConvert.entryToDocument(entries);
                entriesEsSearchService.saveDocument(document);
            } catch (Exception e) {
                // 记录日志，但不影响主流程
                System.err.println("同步写入ES失败: " + e.getMessage());
            }
        }
        
        return result;
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
        // 如果更新为已勾选，则同时设置完成百分比为100；如果更新为未勾选，则同时设置完成百分比为0
        if (request.getChecked() != null) {
            if (request.getChecked() == 1) {
                entries.setCompletionRate(100);
            } else if (request.getChecked() == 0) {
                entries.setCompletionRate(0);
            }
        }
        boolean result = this.update(entries, queryWrapper);
        
        // 双写：同步到 ES
        if (result) {
            try {
                // 查询最新的记录
                Entries updatedEntry = this.getById(request.getId());
                if (updatedEntry != null) {
                    EntriesDocument document = entriesConvert.entryToDocument(updatedEntry);
                    entriesEsSearchService.saveDocument(document);
                }
            } catch (Exception e) {
                // 记录日志，但不影响主流程
                System.err.println("同步更新ES失败: " + e.getMessage());
            }
        }
        
        return result;
    }

    @Override
    public boolean updateEntriesContent(User loginUser, EntriesContentUpdateReq request) {
        // 构建更新条件：当前用户 + 指定ID
        LambdaQueryWrapper<Entries> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entries::getId, request.getId())
                .eq(Entries::getUserId, loginUser.getId());

        // 执行更新
        Entries entries = new Entries();
        entries.setContent(request.getContent());
        entries.setEntryType(request.getEntryType());
        // 如果传入了完成百分比，则同时更新
        if (request.getCompletionRate() != null) {
            entries.setCompletionRate(request.getCompletionRate());
            // 如果完成百分比为100，则自动勾选；否则取消勾选
            if (request.getCompletionRate() == 100) {
                entries.setChecked(1);
            } else {
                entries.setChecked(0);
            }
        }
        boolean result = this.update(entries, queryWrapper);
        
        // 双写：同步到 ES
        if (result) {
            try {
                // 查询最新的记录
                Entries updatedEntry = this.getById(request.getId());
                if (updatedEntry != null) {
                    EntriesDocument document = entriesConvert.entryToDocument(updatedEntry);
                    entriesEsSearchService.saveDocument(document);
                }
            } catch (Exception e) {
                // 记录日志，但不影响主流程
                System.err.println("同步更新ES失败: " + e.getMessage());
            }
        }
        
        return result;
    }

    @Override
    public boolean deleteEntry(User loginUser, EntriesDeleteReq request) {
        // 构建删除条件：当前用户 + 指定ID
        LambdaQueryWrapper<Entries> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entries::getId, request.getId())
                .eq(Entries::getUserId, loginUser.getId());

        // 执行逻辑删除（由于实体类使用了@TableLogic注解）
        boolean result = this.remove(queryWrapper);
        
        // 双写：同步到 ES
        if (result) {
            try {
                // 逻辑删除时，更新ES中的isDelete字段为true
                Entries deletedEntry = this.getById(request.getId());
                if (deletedEntry != null) {
                    EntriesDocument document = entriesConvert.entryToDocument(deletedEntry);
                    entriesEsSearchService.saveDocument(document);
                }
            } catch (Exception e) {
                // 记录日志，但不影响主流程
                System.err.println("同步删除ES失败: " + e.getMessage());
            }
        }
        
        return result;
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
