package com.hxy.service;

import com.hxy.model.AddressDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hxy.request.AddressAddRequest;
import com.hxy.utils.JsonData;

/**
 * <p>
 * 电商-公司收发货地址表 服务类
 * </p>
 *
 * @author zyd
 * @since 2026-05-13
 */
public interface AddressService extends IService<AddressDO> {

    JsonData add(AddressAddRequest address);

    JsonData find(Long id);

    JsonData del(Long id);

    JsonData listaddr();
}
