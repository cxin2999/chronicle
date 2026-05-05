package com.cxin.chronicle.repository;

import com.cxin.chronicle.infrastructure.model.document.EntriesDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Entries Elasticsearch Repository
 * 提供基础的 CRUD 和查询方法
 *
 * @author Charles Chen
 * @since 2026-05-03
 */
@Repository
public interface EntriesRepository extends ElasticsearchRepository<EntriesDocument, Long> {

    /**
     * 根据用户ID查询记录
     */
    List<EntriesDocument> findByUserId(Long userId);

    /**
     * 根据用户ID和类型查询记录
     */
    List<EntriesDocument> findByUserIdAndEntryType(Long userId, String entryType);

    /**
     * 根据用户ID和删除状态查询记录
     */
    List<EntriesDocument> findByUserIdAndIsDelete(Long userId, Boolean isDelete);

    /**
     * 根据内容关键词搜索（全文检索）
     */
    List<EntriesDocument> findByContentContaining(String keyword);

    /**
     * 根据用户ID和内容关键词搜索
     */
    List<EntriesDocument> findByUserIdAndContentContaining(Long userId, String keyword);
}
