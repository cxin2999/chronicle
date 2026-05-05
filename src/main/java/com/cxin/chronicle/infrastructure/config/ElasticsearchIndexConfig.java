package com.cxin.chronicle.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch 索引配置
 *
 * @author Charles Chen
 * @since 2026-05-05
 */
@Data
@Component
@ConfigurationProperties(prefix = "elasticsearch.index")
public class ElasticsearchIndexConfig {

    /**
     * Entries 索引名称
     */
    private String entries = "entries_index_local";
}
