package com.hxy.mapper;

import com.hxy.model.ProductDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zyd
 * @since 2026-05-20
 */
public interface ProductMapper extends BaseMapper<ProductDO> {

    int lockProductStock(@Param("productId") long productId,@Param("buyNum") int buyNum);

    void unlockProductStock(@Param("productId") Long productId,@Param("buyNum") Integer buyNum);
}
