package com.hxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hxy.feign.CouponFeignService;
import com.hxy.interceptor.LoginInterceptor;
import com.hxy.model.LoginUser;
import com.hxy.model.UserDO;
import com.hxy.mapper.UserMapper;
import com.hxy.request.NewUserCouponRequest;
import com.hxy.request.UserLoginRequest;
import com.hxy.request.UserRegisterRequest;
import com.hxy.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxy.utils.CommonUtil;
import com.hxy.utils.JWTUtil;
import com.hxy.utils.JsonData;
import com.hxy.vo.UserVO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zyd
 * @since 2026-05-13
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CouponFeignService couponFeignService;
    
    //实现注册
    @Override

    public JsonData register(UserRegisterRequest user) {
        // 1. 校验邮箱验证码
        String mail = user.getMail();
        if (!StringUtils.isNotBlank( mail)){
            //邮箱号空
            return JsonData.buildError("邮箱号不能为空");
        }
        String string = redisTemplate.opsForValue().get("user-service:code:" + mail);
        String[] split = string.split("-");
        String value = split[0];
        if (!value.equals(user.getCode())){
            return JsonData.buildError("验证码错误");
        }



        //2, 构建UserDO对象
        UserDO userDO = new UserDO();
        userDO.setName(user.getName());
        userDO.setHeadImg(user.getHeadImg());
        userDO.setCreateTime(new Date());
        userDO.setMail(user.getMail());

        //3,验证邮箱是否唯一
        LambdaQueryWrapper<UserDO> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDO::getMail,user.getMail());
        int count = this.count(queryWrapper);
        if (count!=0){
            return JsonData.buildError("重复注册了");
        }



        // 4,密码加密
        String randomString = CommonUtil.getRandomString(8);
        userDO.setSecret("$1$"+randomString);
        String pass = Md5Crypt.md5Crypt(user.getPwd().getBytes(), userDO.getSecret());
        userDO.setPwd(pass);
        // 5,插入数据库   $1$adsd1231
        this.save(userDO);


        //5.1 删除redis验证码
        redisTemplate.delete("user-service:code:" + mail);


        //TODO 6,发放优惠卷
        NewUserCouponRequest request = new NewUserCouponRequest();
        request.setName(userDO.getName());
        request.setUserId(userDO.getId());

        JsonData jsonData = couponFeignService.addNewUserCoupon(request);
        log.info("发放新用户注册优惠券：{}, 结果:{}", request, jsonData);


        return JsonData.buildSuccess();
    }

    @Override
    public JsonData login(UserLoginRequest request) {
        //1,查邮箱是否存在
        LambdaQueryWrapper<UserDO> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDO::getMail,request.getMail());
        UserDO userDO = this.getOne(queryWrapper);
        if (userDO==null){
            return JsonData.buildError("登录失败，用户名或密码不正确");
        }
        //2,验证密码
        String secret = userDO.getSecret();
        String pass = Md5Crypt.md5Crypt(request.getPwd().getBytes(), secret);
        if (!pass.equals(userDO.getPwd())){
            //2,1 失败   返回
            return JsonData.buildError("登录失败，用户名或密码不正确");
        }


        //2.2 成功   生成jwt
        LoginUser loginUser=new LoginUser();
        loginUser.setHeadImg(userDO.getHeadImg());
        loginUser.setId(userDO.getId());
        loginUser.setName(userDO.getName());
        loginUser.setMail(userDO.getMail());

        String jwt = JWTUtil.geneJsonWebToken(loginUser);


        return JsonData.buildSuccess(jwt);
    }

    @Override
    public JsonData detail() {
        //1,获取当前用户
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        //2,查询用户信息
        UserDO userDO = this.getById(loginUser.getId());
        //3,转换vo类型
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO,userVO);
        //4,返回
        return JsonData.buildSuccess(userVO);
    }
}
