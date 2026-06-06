package com.hxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxy.enums.CouponCategoryEnum;
import com.hxy.enums.CouponPublishEnum;
import com.hxy.enums.CouponStateEnum;
import com.hxy.exception.BizException;
import com.hxy.interceptor.LoginInterceptor;
import com.hxy.mapper.CouponRecordMapper;
import com.hxy.model.CouponDO;
import com.hxy.mapper.CouponMapper;
import com.hxy.model.CouponRecordDO;
import com.hxy.service.CouponRecordService;
import com.hxy.service.CouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.CommonUtil;
import com.hxy.utils.JsonData;
import com.hxy.vo.CouponVO;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zyd
 * @since 2026-05-19
 */
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponDO> implements CouponService {

    @Autowired
    private CouponRecordMapper couponRecordMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;
    // ... existing code ...

    /**
     * 分页查询优惠券活动列表
     * <p>
     * 查询已发布的优惠券信息，返回分页数据，包含总记录数和优惠券列表。
     * 将CouponDO转换为CouponVO返回给前端。
     * </p>
     *
     * @param page 页码，从1开始
     * @param size 每页显示条数
     * @return JsonData 返回封装的JSON数据，包含：
     * - total: 总记录数
     * - rows: 当前页的优惠券VO列表
     */
    @Override
    public JsonData pageCouponActivity(int page, int size) {
        // 创建分页对象，设置页码和每页大小
        Page<CouponDO> pageData = new Page<>(page, size);

        // 构建查询条件，只查询已发布的优惠券
        LambdaQueryWrapper<CouponDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponDO::getPublish, CouponPublishEnum.PUBLISH.name());
        Page<CouponDO> pageResult = this.page(pageData, queryWrapper);

        // 封装返回结果，包含总记录数和转换后的VO列表
        Map<String, Object> map = new HashMap<>();
        map.put("total", pageResult.getTotal());

        // 将DO对象转换为VO对象，用于前端展示
        List<CouponVO> collect = pageResult.getRecords().stream().map(aDo -> {
            CouponVO couponvO = new CouponVO();
            BeanUtils.copyProperties(aDo, couponvO);
            return couponvO;
        }).collect(Collectors.toList());

        map.put("rows", collect);


        return JsonData.buildSuccess(map);
    }


// ... existing code ...


    // ... existing code ...

    /**
     * 用户领取促销优惠券
     * <p>
     * 处理用户点击领券的完整流程：
     * 1. 校验优惠券是否存在且已发布
     * 2. 校验当前时间是否在优惠券有效期内
     * 3. 校验库存是否充足
     * 4. 校验用户是否超过限领数量
     * 5. 扣减库存并保存领券记录
     * </p>
     *

     * @return JsonData 返回封装的JSON数据，成功时返回空数据
     * @throws BizException 当优惠券不存在、已下架、已过期、库存不足或超过限领数量时抛出业务异常
     */
//    @Override
//    public JsonData addPromotionCoupon(Long couponId) {
//            /*用户点击领券
//                ↓
//            校验券是否存在且已发布
//                ↓
//            校验有效期：当前时间是否在 (start_time, end_time) 之间
//                ↓
//            校验库存：stock > 0 ？
//                ↓
//            校验限领：该用户已领数量 < user_limit ？
//                ↓
//            扣减库存：stock = stock - 1
//                ↓
//            保存领券记录
//                ↓
//            返回成功*/
//        //向redis存值 加锁
//        String uuid = CommonUtil.generateUUID();
//        String lockKey = "lock:coupon:" + couponId;
//        // 原子加锁，有效期30秒
//        Boolean lockSuccess = redisTemplate.opsForValue()
//                .setIfAbsent(lockKey, uuid, Duration.ofSeconds(30));
//        if (lockSuccess){
//            // 锁获取成功
//            CouponDO couponDO = this.getById(couponId);
//            this.checkCoupon(couponDO);
//
//
//            // 通过数据库原子操作扣减库存，确保并发安全
//            couponMapper.reduceStock(couponId);
//
//
//            CouponRecordDO couponRecordDO = new CouponRecordDO();
//            couponRecordDO.setCouponId(couponDO.getId());
//            couponRecordDO.setUserId(LoginInterceptor.threadLocal.get().getId());
//            couponRecordDO.setUserName(LoginInterceptor.threadLocal.get().getName());
//            couponRecordDO.setCouponTitle(couponDO.getCouponTitle());
//            couponRecordDO.setStartTime(couponDO.getStartTime());
//            couponRecordDO.setEndTime(couponDO.getEndTime());
//            couponRecordDO.setCreateTime(new Date());
//            couponRecordDO.setUseState(CouponStateEnum.NEW.name());
//            couponRecordMapper.insert(couponRecordDO);
//
//
//            // Lua原子解锁
//            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
//            redisTemplate.execute(
//                    new DefaultRedisScript<>(script, Long.class),
//                    Collections.singletonList(lockKey),
//                    uuid
//            );
//        }else {
//            // 锁未获取成功
//            // 获取锁失败，休眠100ms自旋重试
//            try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) {}
//            return addPromotionCoupon(couponId); // 递归重试
//        }
//
//
//
//
//
//
//
//
//
//
//
//        return JsonData.buildSuccess();
//    }

    // ... existing code ...


    // 校验券
    private void checkCoupon(CouponDO couponDO) {
        Long id = LoginInterceptor.threadLocal.get().getId();

        // 券不存在
        if (couponDO == null) {
            new BizException(BizCodeEnum.COUPON_NOT_EXIST);
        }

        // 券已下架
        if (!CouponPublishEnum.PUBLISH.name().equals(couponDO.getPublish())) {
            new BizException(BizCodeEnum.COUPON_NOT_USABLE);
        }

        // 不在有效时间范围
        long now = System.currentTimeMillis();
        if (now < couponDO.getStartTime().getTime() ||
                now > couponDO.getEndTime().getTime()) {
            throw new BizException(BizCodeEnum.COUPON_EXPIRED);
        }

        // 库存不足
        if (couponDO.getStock() <= 0) {
            new BizException(BizCodeEnum.COUPON_NOT_ENOUGH);
        }

        // 超过每人限领次数
        int count = couponRecordMapper.selectCount(
                new QueryWrapper<CouponRecordDO>()
                        .eq("coupon_id", couponDO.getId())
                        .eq("user_id", id));
        if (count >= couponDO.getUserLimit()) {
            throw new BizException(BizCodeEnum.COUPON_NOT_EXIST_USER);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData addPromotionCoupon(Long couponId) {
        // 向redis存值 加锁
        redissonClient.getLock("lock:coupon:" + couponId).lock();
        try {

            //Thread.sleep(1000000);
            // 锁获取成功
            CouponDO couponDO = this.getById(couponId);
            this.checkCoupon(couponDO);


            // 通过数据库原子操作扣减库存，确保并发安全
            couponMapper.reduceStock(couponId);

            //int i=10/0;


            CouponRecordDO couponRecordDO = new CouponRecordDO();
            couponRecordDO.setCouponId(couponDO.getId());
            couponRecordDO.setUserId(LoginInterceptor.threadLocal.get().getId());
            couponRecordDO.setUserName(LoginInterceptor.threadLocal.get().getName());
            couponRecordDO.setCouponTitle(couponDO.getCouponTitle());
            couponRecordDO.setStartTime(couponDO.getStartTime());
            couponRecordDO.setEndTime(couponDO.getEndTime());
            couponRecordDO.setCreateTime(new Date());
            couponRecordDO.setUseState(CouponStateEnum.NEW.name());
            couponRecordMapper.insert(couponRecordDO);
        }catch (Exception e){
                throw e;
        }finally {
            // Lua原子解锁
            redissonClient.getLock("lock:coupon:" + couponId).unlock();
        }

        return JsonData.buildSuccess();
    }
}
