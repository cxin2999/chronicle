package com.cxin.chronicle.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.cxin.chronicle.infrastructure.convert.EntriesConvert;
import com.cxin.chronicle.infrastructure.model.document.EntriesDocument;
import com.cxin.chronicle.infrastructure.model.dto.entries.EntriesSearchReq;
import com.cxin.chronicle.infrastructure.model.vo.EntriesVo;
import com.cxin.chronicle.infrastructure.model.vo.PageResponse;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 搜索服务
 * 演示如何使用注解实体进行 ES 操作
 *
 * @author Charles Chen
 * @since 2026-05-03
 */
@Service
public class EntriesEsSearchService {

    @Resource
    private com.cxin.chronicle.repository.EntriesRepository entriesRepository;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Resource
    private EntriesConvert entriesConvert;

    /**
     * 全文搜索（使用 IK 分词器）
     *
     * @param keyword 搜索关键词
     * @return 匹配的文档列表
     */
    public List<EntriesDocument> searchByContent(String keyword) {
        // Spring Data Elasticsearch 会自动使用注解中配置的分词器
        return entriesRepository.findByContentContaining(keyword);
    }

    /**
     * 根据用户ID搜索
     *
     * @param userId  用户ID
     * @param keyword 关键词
     * @return 匹配的文档列表
     */
    public List<EntriesDocument> searchByUserAndKeyword(Long userId, String keyword) {
        return entriesRepository.findByUserIdAndContentContaining(userId, keyword);
    }

    /**
     * 高级搜索 - 使用 Native Query
     * 支持更复杂的查询条件
     *
     * @param keyword   关键词
     * @param entryType 类型过滤
     * @param pageable  分页参数
     * @return 分页结果
     */
    public Page<EntriesDocument> advancedSearch(String keyword, String entryType, Pageable pageable) {
        // 构建查询条件
        NativeQueryBuilder queryBuilder = NativeQuery.builder();
        // 关键词搜索（使用 IK 分词）
        if (keyword != null && !keyword.isEmpty()) {
            Query matchQuery = Query.of(q -> q
                    .match(m -> m
                            .field("content")
                            .query(keyword)
                    )
            );
            queryBuilder.withQuery(matchQuery);
        }

        // 类型过滤
        if (entryType != null && !entryType.isEmpty()) {
            Query termQuery = Query.of(q -> q
                    .term(t -> t
                            .field("entryType")
                            .value(entryType)
                    )
            );
            queryBuilder.withFilter(termQuery);
        }

        // 只查询未删除的记录
        Query deleteFilter = Query.of(q -> q
                .term(t -> t
                        .field("isDelete")
                        .value(false)
                )
        );
        queryBuilder.withFilter(deleteFilter);

        NativeQuery query = queryBuilder.build();

        // 执行查询
        SearchHits<EntriesDocument> searchHits = elasticsearchOperations.search(query, EntriesDocument.class);

        // 转换为 Page
        List<EntriesDocument> documents = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(documents, pageable, searchHits.getTotalHits());
    }

    /**
     * 高级搜索 - 基于用户和请求对象
     * 支持关键词、类型过滤、分页、排序
     *
     * @param userId  用户ID
     * @param request 搜索请求
     * @return 分页搜索结果
     */
    public PageResponse<EntriesVo> advancedSearch(String userId, EntriesSearchReq request) {
        int pageNum = request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() > 0 ? request.getPageSize() : 10;
        
        Sort sort = buildSort(request.getSortField(), request.getSortOrder());
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withPageable(pageable);

        // 构建布尔查询
        Query boolQuery = Query.of(q -> q
                .bool(b -> {
                    // 必须匹配的条件
                    b.must(m -> m
                            .term(t -> t
                                    .field("userId")
                                    .value(Long.parseLong(userId))
                            )
                    );
                    
                    b.must(m -> m
                            .term(t -> t
                                    .field("isDelete")
                                    .value(false)
                            )
                    );

                    // 关键词搜索（使用 IK 分词）
                    if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                        b.must(m -> m
                                .match(match -> match
                                        .field("content")
                                        .query(request.getKeyword())
                                )
                        );
                    }

                    // 类型过滤
                    if (request.getEntryType() != null && !request.getEntryType().isEmpty()) {
                        b.filter(f -> f
                                .term(t -> t
                                        .field("entryType")
                                        .value(request.getEntryType())
                                )
                        );
                    }

                    return b;
                })
        );

        queryBuilder.withQuery(boolQuery);

        NativeQuery query = queryBuilder.build();

        // 执行查询
        SearchHits<EntriesDocument> searchHits = elasticsearchOperations.search(query, EntriesDocument.class);

        // 获取总记录数
        long total = searchHits.getTotalHits();

        // 转换文档为VO
        List<SearchHit<EntriesDocument>> searchHits1 = searchHits.getSearchHits();
        List<EntriesDocument> documents = searchHits1.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        List<EntriesVo> entriesVoList = entriesConvert.documentsToVoList(documents);

        // 构建分页响应
        return new PageResponse<>(entriesVoList, total, pageNum, pageSize);
    }

    /**
     * 构建排序对象
     *
     * @param sortField 排序字段
     * @param sortOrder 排序顺序（ascend/descend）
     * @return Sort对象
     */
    private Sort buildSort(String sortField, String sortOrder) {
        if (sortField == null || sortField.isEmpty()) {
            sortField = "createTime";
        }
        
        Sort.Direction direction = "ascend".equalsIgnoreCase(sortOrder) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        return Sort.by(direction, sortField);
    }

    /**
     * 保存或更新文档
     *
     * @param document 文档对象
     * @return 保存后的文档
     */
    public EntriesDocument saveDocument(EntriesDocument document) {
        return entriesRepository.save(document);
    }

    /**
     * 批量保存文档
     *
     * @param documents 文档列表
     * @return 保存后的文档列表
     */
    public Iterable<EntriesDocument> saveAllDocuments(Iterable<EntriesDocument> documents) {
        return entriesRepository.saveAll(documents);
    }

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    public void deleteDocument(Long id) {
        entriesRepository.deleteById(id);
    }

    /**
     * 根据ID查询文档
     *
     * @param id 文档ID
     * @return 文档对象
     */
    public EntriesDocument getDocumentById(Long id) {
        return entriesRepository.findById(id).orElse(null);
    }
}
