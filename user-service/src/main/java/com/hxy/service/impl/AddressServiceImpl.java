package com.hxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxy.interceptor.LoginInterceptor;
import com.hxy.model.AddressDO;
import com.hxy.mapper.AddressMapper;
import com.hxy.request.AddressAddRequest;
import com.hxy.service.AddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.JsonData;
import com.hxy.vo.AddressVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 电商-公司收发货地址表 服务实现类
 * </p>
 *
 * @author zyd
 * @since 2026-05-13
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressDO> implements AddressService {

    @Override
    public JsonData add(AddressAddRequest address) {
        //1,验证参数
        //2,补全参数
        AddressDO addressDO = new AddressDO();
        BeanUtils.copyProperties(address, addressDO);
        addressDO.setUserId(LoginInterceptor.threadLocal.get().getId());
        addressDO.setCreateTime(new Date());


        //3,是不是默认收货地址
        if (address.getDefaultStatus() == 1) {
            LambdaQueryWrapper<AddressDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AddressDO::getUserId, LoginInterceptor.threadLocal.get().getId());
            queryWrapper.eq(AddressDO::getDefaultStatus, 1);
            AddressDO aDo = getOne(queryWrapper);

            if (aDo != null) {
                aDo.setDefaultStatus(0);
                updateById(aDo);
            }
        }
        save(addressDO);

        return JsonData.buildSuccess();
    }

    @Override
    public JsonData find(Long id) {
        //解决越权共计
        Long id1 = LoginInterceptor.threadLocal.get().getId();
        LambdaQueryWrapper<AddressDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressDO::getId, id);
        queryWrapper.eq(AddressDO::getUserId, id1);
        AddressDO aDo = getOne(queryWrapper);
        //select * from address where id=? and user_id=1
        if (aDo == null) {
            return JsonData.buildResult(BizCodeEnum.USER_ADDRESS_NOT_EXIST);
        }

        AddressVO addressVO=new AddressVO();
        BeanUtils.copyProperties(aDo, addressVO);
        return JsonData.buildSuccess(addressVO);
    }

    @Override
    public JsonData del(Long id) {
        Long id1 = LoginInterceptor.threadLocal.get().getId();
        LambdaQueryWrapper<AddressDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressDO::getId, id);
        queryWrapper.eq(AddressDO::getUserId, id1);


        remove(queryWrapper);
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData listaddr() {
        Long id = LoginInterceptor.threadLocal.get().getId();
        LambdaQueryWrapper<AddressDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressDO::getUserId, id);
        List<AddressDO> list = list(queryWrapper);

        List<AddressVO> collect = list.stream().map(aDo -> {
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(aDo, addressVO);
            return addressVO;
        }).collect(Collectors.toList());

        return JsonData.buildSuccess( collect);
    }
}
