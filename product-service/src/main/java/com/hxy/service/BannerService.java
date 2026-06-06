package com.hxy.service;

import com.hxy.model.BannerDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hxy.utils.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zyd
 * @since 2026-05-20
 */
public interface BannerService extends IService<BannerDO> {

    JsonData lists();
}
