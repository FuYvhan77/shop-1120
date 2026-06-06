package com.hxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxy.config.RabbitMQConfig;
import com.hxy.enums.ProductOrderStateEnum;
import com.hxy.enums.ProductTaskStateEnum;
import com.hxy.exception.BizException;
import com.hxy.feign.ProductOrderFeignSerivce;
import com.hxy.mapper.ProductTaskMapper;
import com.hxy.model.ProductDO;
import com.hxy.mapper.ProductMapper;
import com.hxy.model.ProductDocument;
import com.hxy.model.ProductMessage;
import com.hxy.model.ProductTaskDO;
import com.hxy.request.LockProductRequest;
import com.hxy.request.OrderItemRequest;
import com.hxy.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.JsonData;
import com.hxy.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zyd
 * @since 2026-05-20
 */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductTaskMapper productTaskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private ProductOrderFeignSerivce productOrderFeignSerivce;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Map<String, Object> page1(int page, int size) {
        Page<ProductDO> pageInfo = new Page<>(page, size);
        Page<ProductDO> pageResult = page(pageInfo, null);


        Map<String, Object> pageMap = new HashMap<>();
        pageMap.put("total_record", pageResult.getTotal());
        pageMap.put("total_page", pageResult.getPages());
        pageMap.put("current_data", pageResult.getRecords().stream()
                .map(this::beanProcess).collect(Collectors.toList()));
        return pageMap;
    }

    @Override
    public ProductVO findDetailById(long productId) {
        ProductDO productDO = productMapper.selectById(productId);
        return beanProcess(productDO);
    }


    private ProductVO beanProcess(ProductDO productDO) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(productDO, vo);
        // 关键：前端展示的库存 = 实际库存 - 已锁定库存
        vo.setStock(productDO.getStock() - productDO.getLockStock());
        return vo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData lockProductStock(LockProductRequest lockProductRequest) {
        //1,获取数据
        String orderOutTradeNo = lockProductRequest.getOrderOutTradeNo();
        List<OrderItemRequest> orderItemList = lockProductRequest.getOrderItemList();

        //2,查询商品
        List<Long> ids = orderItemList.stream().map(item -> {
            long productId = item.getProductId();
            return productId;
        }).collect(Collectors.toList());
        List<ProductDO> productDOS = this.listByIds(ids);

        //3,处理数据把list转成map  id，商品数
        Map<Long, ProductDO> map = productDOS.stream().collect(Collectors.toMap(ProductDO::getId, productDO -> productDO));


        //4,扣减库存
        for (OrderItemRequest orderItemRequest : orderItemList) {
            int count = productMapper.lockProductStock(orderItemRequest.getProductId(), orderItemRequest.getBuyNum());
            if (count == 0) {
                log.info("库存不足");
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
            }

            // 锁定成功后插入锁定任务
            ProductDO productDO = map.get(orderItemRequest.getProductId());
            ProductTaskDO taskDO = new ProductTaskDO();
            taskDO.setBuyNum(orderItemRequest.getBuyNum());
            taskDO.setLockState(ProductTaskStateEnum.LOCK.name());
            taskDO.setProductId(orderItemRequest.getProductId());
            taskDO.setProductName(productDO.getTitle());
            taskDO.setOutTradeNo(orderOutTradeNo);
            productTaskMapper.insert(taskDO);


            //发送消息
            ProductMessage message = new ProductMessage();
            message.setOutTradeNo(orderOutTradeNo);
            message.setTaskId(taskDO.getId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getStockReleaseDelayRoutingKey(), message);
        }


        return JsonData.buildSuccess();
    }

    @Override
    public boolean releaseProductStock(ProductMessage productMessage) {
        //1,查询任务
        ProductTaskDO taskDO = productTaskMapper.selectById(productMessage.getTaskId());
        //2,判断任务状态
        if (taskDO == null) {
            log.info("任务不存在");
            return true;
        }
        //3,判断任务状态
        if (!taskDO.getLockState().equals(ProductTaskStateEnum.LOCK.name())) {
            log.info("任务状态不是锁定状态");
            return true;
        }
        //4,查询订单状态
        JsonData jsonData = productOrderFeignSerivce.queryProductOrderState(productMessage.getOutTradeNo());
        if (jsonData.getCode() == 0) {
            Object data = jsonData.getData();
            String status = (String) data;

            if (status.equals(ProductOrderStateEnum.NEW.name())){
                log.info("订单未支付，准备释放库存");
                return false;
            }

            if (status.equals(ProductOrderStateEnum.PAY.name())){
                log.info("订单已支付");
                LambdaUpdateWrapper<ProductTaskDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(ProductTaskDO::getId, taskDO.getId());
                updateWrapper.set(ProductTaskDO::getLockState, ProductTaskStateEnum.FINISH.name());
                productTaskMapper.update(null, updateWrapper);
                return true;
            }
        }
        //订单失败，准备释放库存
        LambdaUpdateWrapper<ProductTaskDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductTaskDO::getId, taskDO.getId());
        updateWrapper.set(ProductTaskDO::getLockState, ProductTaskStateEnum.CANCEL.name());
        productTaskMapper.update(null, updateWrapper);

        //5,释放库存
        productMapper.unlockProductStock(taskDO.getProductId(), taskDO.getBuyNum());
        log.warn("订单不存在或已取消，释放商品库存, task=CANCEL, message={}", productMessage);
        return true;
    }

    @Override
    public JsonData saveproductDO(ProductDO productDO) {
        //1,myssql添加
        productDO.setCreateTime(new Date());
        productDO.setLockStock(0);
        save(productDO);
        //2,es添加

        syncToES(productDO);
        return JsonData.buildSuccess();
    }



    @Async//异步方法
    public void syncToES(ProductDO productDO) {
        try {
            ProductDocument doc = convertToDocument(productDO);
            elasticsearchRestTemplate.save(doc);
            log.info("同步商品到ES成功，productId={}", productDO.getId());
        } catch (Exception e) {
            log.error("同步商品到ES失败，productId={}", productDO.getId(), e);
            // 记录失败日志，等待补偿
        }
    }


    private ProductDocument convertToDocument(ProductDO product) {
        ProductDocument doc = new ProductDocument();
        BeanUtils.copyProperties(product, doc);
        // 格式化时间字段
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        doc.setCreateTime(sdf.format(product.getCreateTime()));
        return doc;
    }



    //去es搜索对应的数据
    @Override
    public JsonData searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        //1,构造查询条件
        // 构建布尔查询（组合多个条件）
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(keyword)) {
            // 添加匹配查询条件
            boolQuery.must(QueryBuilders.matchQuery("title", keyword));
        }
        if (minPrice != null) {
            // 添加价格区间查询条件
            boolQuery.filter(QueryBuilders.rangeQuery("amount").gte(minPrice));
        }
        if (maxPrice != null) {
            // 添加价格区间查询条件
            boolQuery.filter(QueryBuilders.rangeQuery("amount").lte(maxPrice));
        }
        // 如果既没有关键词也没有价格筛选，返回所有商品（可加size限制）
        if (StringUtils.isBlank(keyword) && minPrice == null && maxPrice == null) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }

        // 高亮配置
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title")
                .preTags("<em>")      // 高亮标签
                .postTags("</em>");

        // 构建查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withHighlightBuilder(highlightBuilder)
                .withPageable(PageRequest.of(0, 20))   // 默认返回前20条
                .build();


        // 执行查询
        SearchHits<ProductDocument> searchHits = elasticsearchRestTemplate.search(
                searchQuery, ProductDocument.class);

        // 处理返回结果
        List<ProductDocument> productList = searchHits.stream().map(hit -> {
            ProductDocument doc = hit.getContent();
            // 替换高亮字段
            List<String> highlights = hit.getHighlightField("title");
            if (highlights != null && !highlights.isEmpty()) {
                doc.setTitle(highlights.get(0));  // 将原始标题替换为高亮后的标题
            }
            return doc;
        }).collect(Collectors.toList());

        return JsonData.buildSuccess(productList);
    }
}
