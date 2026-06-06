package com.hxy.config;

import com.hxy.model.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

import javax.annotation.PostConstruct;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName InitIndex
 * @date 2026-06-02 13:43
 */

@Slf4j
@Configuration
public class InitIndex {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    @PostConstruct
    public void initIndex() {
        log.info("初始化ES索引");
        try {
            IndexOperations ops = elasticsearchRestTemplate.indexOps(ProductDocument.class);
            if (!ops.exists()) {
                ops.create();
                ops.putMapping(ops.createMapping());
                log.info("ES索引 product_index 创建成功");
            }
        } catch (Exception e) {
            log.error("初始化ES索引失败", e);
        }
    }
}
