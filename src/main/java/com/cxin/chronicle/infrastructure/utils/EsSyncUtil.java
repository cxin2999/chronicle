package com.cxin.chronicle.infrastructure.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cxin.chronicle.infrastructure.model.document.EntriesDocument;
import com.cxin.chronicle.infrastructure.model.entity.Entries;
import com.cxin.chronicle.mapper.EntriesMapper;
import com.cxin.chronicle.repository.EntriesRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Elasticsearch 数据同步工具
 *
 * @author Charles Chen
 * @since 2026-05-03
 */
@Slf4j
@Component
public class EsSyncUtil {

    @Resource
    private EntriesMapper entriesMapper;

    @Resource
    private EntriesRepository entriesRepository;

    /**
     * 全量同步所有未删除的记录到 ES
     *
     * @return 同步成功的记录数
     */
    public int syncAllToEs() {
        log.info("开始全量同步数据到 Elasticsearch...");

        // 查询所有未删除的记录
        LambdaQueryWrapper<Entries> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entries::getIsDelete, 0);
        List<Entries> entriesList = entriesMapper.selectList(queryWrapper);

        log.info("查询到 {} 条待同步记录", entriesList.size());

        int successCount = 0;
        int failCount = 0;

        for (Entries entry : entriesList) {
            try {
                EntriesDocument document = convertToDocument(entry);
                entriesRepository.save(document);
                successCount++;
                
                if (successCount % 100 == 0) {
                    log.info("已同步 {} 条记录", successCount);
                }
            } catch (Exception e) {
                failCount++;
                log.error("同步记录失败, id: {}", entry.getId(), e);
            }
        }

        log.info("同步完成！成功: {}, 失败: {}", successCount, failCount);
        return successCount;
    }

    /**
     * 将 MySQL 实体转换为 ES 文档
     */
    private EntriesDocument convertToDocument(Entries entry) {
        EntriesDocument document = new EntriesDocument();
        document.setId(Long.parseLong(entry.getId()));
        document.setUserId(Long.parseLong(entry.getUserId()));
        document.setContent(entry.getContent());
        document.setEntryType(entry.getEntryType());
        document.setChecked(entry.getChecked() == 1);
        document.setCompletionRate(entry.getCompletionRate());
        document.setCreateTime(entry.getCreateTime());
        document.setUpdateTime(entry.getUpdateTime());
        document.setIsDelete(entry.getIsDelete() == 1);
        document.setContentRaw(entry.getContent()); // 用于精确匹配
        return document;
    }

    /**
     * 批量同步（使用 Repository 的 saveAll）
     *
     * @return 同步成功的记录数
     */
    public int bulkSyncToEs() {
        log.info("开始批量同步数据到 Elasticsearch...");

        // 查询所有未删除的记录
        LambdaQueryWrapper<Entries> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entries::getIsDelete, 0);
        List<Entries> entriesList = entriesMapper.selectList(queryWrapper);

        log.info("查询到 {} 条待同步记录", entriesList.size());

        // 转换为 ES 文档
        List<EntriesDocument> documents = entriesList.stream()
                .map(this::convertToDocument)
                .toList();

        // 分批处理，每批 500 条
        int batchSize = 500;
        int totalSuccess = 0;

        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<EntriesDocument> batch = documents.subList(i, end);

            try {
                entriesRepository.saveAll(batch);
                totalSuccess += batch.size();
                log.info("批次同步完成: {}/{}, 本批成功: {}", end, documents.size(), batch.size());
            } catch (Exception e) {
                log.error("批次同步失败, 范围: {}-{}", i, end, e);
            }
        }

        log.info("批量同步完成！总共成功: {}", totalSuccess);
        return totalSuccess;
    }

    /**
     * 从 ES 中删除记录
     *
     * @param entryId 记录ID
     */
    public void deleteFromEs(String entryId) {
        try {
            entriesRepository.deleteById(Long.parseLong(entryId));
            log.info("从 ES 删除记录成功, id: {}", entryId);
        } catch (Exception e) {
            log.warn("从 ES 删除记录失败, id: {}", entryId, e);
        }
    }
}
