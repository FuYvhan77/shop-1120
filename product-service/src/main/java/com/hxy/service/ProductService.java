package com.hxy.service;

import com.hxy.model.ProductDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hxy.model.ProductMessage;
import com.hxy.request.LockProductRequest;
import com.hxy.utils.JsonData;
import com.hxy.vo.ProductVO;

import java.math.BigDecimal;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zyd
 * @since 2026-05-20
 */
public interface ProductService extends IService<ProductDO> {

    Object page1(int page, int size);

    ProductVO findDetailById(long productId);

    JsonData lockProductStock(LockProductRequest lockProductRequest);

    boolean releaseProductStock(ProductMessage productMessage);

    JsonData saveproductDO(ProductDO productDO);

    JsonData searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice);
}
