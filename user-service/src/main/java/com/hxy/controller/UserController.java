package com.hxy.controller;


import com.hxy.interceptor.LoginInterceptor;
import com.hxy.model.LoginUser;
import com.hxy.request.UserLoginRequest;
import com.hxy.request.UserRegisterRequest;
import com.hxy.service.FileService;
import com.hxy.service.UserService;
import com.hxy.utils.JsonData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zyd
 * @since 2026-05-13
 */

@Api(tags = "用户接口")
@RestController
@RequestMapping("/api/user/v1")
public class UserController {


    @Autowired
    private FileService fileService;


    @Autowired
    private UserService userService;

    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public JsonData upload(@ApiParam(value = "文件", required = true)
                           @RequestPart("file") MultipartFile file) {
        return fileService.upload(file);
    }


    @PostMapping("/register")
    @ApiOperation("注册")
    public JsonData register(@ApiParam (value = "用户注册对象", required = true) @RequestBody UserRegisterRequest userRegisterRequest) {

        return userService.register(userRegisterRequest);
    }




    @PostMapping("/login")
    @ApiOperation("登录")
    public JsonData login(@ApiParam (value = "用户登录对象", required = true) @RequestBody UserLoginRequest request) {
        return userService.login(request);
    }



    @GetMapping("/detail")
    @ApiOperation("获取用户信息")
    public JsonData detail() {
        return userService.detail();
    }

}

