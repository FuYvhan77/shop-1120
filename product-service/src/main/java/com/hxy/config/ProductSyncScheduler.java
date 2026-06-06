package com.hxy.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxy.model.ProductDO;
import com.hxy.model.ProductDocument;
import com.hxy.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName ProductSyncScheduler
 * @date 2026-06-02 13:59
 */

@Component
@Slf4j
public class ProductSyncScheduler {

    @Autowired
    private ProductService productService;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Scheduled(cron = "0/5 * * * * ?")
    public void syncToES() {
        try {
            int pageSize = 100;
            int pageNum = 1;
            boolean hasMore = true;

            while (hasMore) {
                // 分页查询最近24小时内更新的商品
                Page<ProductDO> page = new Page<>(pageNum, pageSize);
                LambdaQueryWrapper<ProductDO> wrapper = Wrappers.lambdaQuery();
                wrapper.ge(ProductDO::getCreateTime, LocalDateTime.now().minusDays(1));
                Page<ProductDO> productPage = productService.page(page, wrapper);

                List<ProductDO> records = productPage.getRecords();
                if (records.isEmpty()) {
                    hasMore = false;
                } else {
                    // 批量更新ES
                    List<ProductDocument> docs = records.stream().map(this::convert).collect(Collectors.toList());
                    elasticsearchRestTemplate.save(docs);
                    pageNum++;
                }
            }
        } catch (Exception e) {
            log.error("定时同步ES失败", e);
        }


    }

    private ProductDocument convert(ProductDO product) {
        ProductDocument doc = new ProductDocument();
        BeanUtils.copyProperties(product, doc);
        // ...
        return doc;
    }
}
