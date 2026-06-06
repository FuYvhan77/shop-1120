package com.hxy.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data//添加getter和setter方法
@ApiModel(value = "登录对象", description = "用户登录请求对象")
public class UserLoginRequest {
    @ApiModelProperty(value = "邮箱", example = "3194610592@qq.com")
    private String mail;

    @ApiModelProperty(value = "密码", example = "12345")
    private String pwd;
}