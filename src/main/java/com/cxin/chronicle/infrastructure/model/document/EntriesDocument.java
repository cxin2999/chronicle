package com.cxin.chronicle.infrastructure.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entries Elasticsearch 文档实体
 * 通过注解映射 ES 索引结构
 *
 * @author Charles Chen
 * @since 2026-05-03
 */
@Data
@Document(indexName = "#{@elasticsearchIndexConfig.entries}", createIndex = false)
public class EntriesDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @Id
    @Field(type = FieldType.Long)
    private Long id;

    /**
     * 用户ID
     */
    @Field(type = FieldType.Long)
    private Long userId;

    /**
     * 记录内容（支持中文全文检索）
     */
    @Field(
        type = FieldType.Text,
        analyzer = "ik_max_word",      // 索引时使用最细粒度分词
        searchAnalyzer = "ik_smart"    // 搜索时使用智能分词
    )
    private String content;

    /**
     * 记录类型（精确匹配，不分词）
     */
    @Field(type = FieldType.Keyword)
    private String entryType;

    /**
     * 是否勾选
     */
    @Field(type = FieldType.Boolean)
    private Boolean checked;

    /**
     * 完成百分比(0-100)
     */
    @Field(type = FieldType.Integer)
    private Integer completionRate;

    /**
     * 创建时间
     */
    @Field(
        type = FieldType.Date,
        format = DateFormat.date_hour_minute_second,
        pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Field(
        type = FieldType.Date,
        format = DateFormat.date_hour_minute_second,
        pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private LocalDateTime updateTime;

    /**
     * 是否删除（逻辑删除标记）
     */
    @Field(type = FieldType.Boolean)
    private Boolean isDelete;

    /**
     * 原始内容字段（用于精确匹配、排序、聚合）
     * 这是 content 的多字段映射
     */
    @Field(
        type = FieldType.Keyword,
        ignoreAbove = 512
    )
    private String contentRaw;
}
