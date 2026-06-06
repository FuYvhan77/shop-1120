package com.hxy.mapper;

import com.hxy.model.CouponTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zyd
 * @since 2026-05-26
 */
public interface CouponTaskMapper extends BaseMapper<CouponTaskDO> {

    int insertBatch(@Param("couponTaskList") List<CouponTaskDO> couponTaskList);
}
