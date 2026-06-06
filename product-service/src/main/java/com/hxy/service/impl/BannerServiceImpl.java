package com.hxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxy.model.BannerDO;
import com.hxy.mapper.BannerMapper;
import com.hxy.service.BannerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxy.utils.JsonData;
import com.hxy.vo.BannerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zyd
 * @since 2026-05-20
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, BannerDO> implements BannerService {

    @Override
    public JsonData lists() {
        //1,定义条件构造器
        LambdaQueryWrapper<BannerDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(BannerDO::getWeight);
        List<BannerDO> list = list(queryWrapper);

        //2,转vo
        List<BannerVO> collect = list.stream().map(bannerDO -> {
            BannerVO bannerVO = new BannerVO();
            BeanUtils.copyProperties(bannerDO, bannerVO);
            return bannerVO;
        }).collect(Collectors.toList());
        return JsonData.buildSuccess(collect);
    }
}
