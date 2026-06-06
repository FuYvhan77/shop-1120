package com.hxy.mapper;

import com.hxy.model.CouponRecordDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zyd
 * @since 2026-05-19
 */
public interface CouponRecordMapper extends BaseMapper<CouponRecordDO> {

    int lockUseStateBatch(@Param("userId") Long id, @Param("useState") String name, @Param("lockCouponRecordIds")List<Long> lockCouponRecordIds);

    void updateState(@Param("couponRecordId") Long couponRecordId,@Param("useState") String name);
}
