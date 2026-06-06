package com.hxy.mapper;

import com.hxy.model.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zyd
 * @since 2026-05-19
 */
public interface CouponMapper extends BaseMapper<CouponDO> {

    int reduceStock( Long id);
}
