package com.hxy.service;

import com.hxy.model.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hxy.request.UserLoginRequest;
import com.hxy.request.UserRegisterRequest;
import com.hxy.utils.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zyd
 * @since 2026-05-13
 */
public interface UserService extends IService<UserDO> {

    JsonData register(UserRegisterRequest userRegisterRequest);

    JsonData login(UserLoginRequest request);

    JsonData detail();
}
