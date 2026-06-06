package com.hxy.controller;


import com.hxy.service.BannerService;
import com.hxy.utils.JsonData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zyd
 * @since 2026-05-20
 */
@Api(tags = "轮播图")
@RestController
@RequestMapping("/api/banner/v1")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @ApiOperation("查询所有")
    @GetMapping("list")
    public JsonData list() {
        return bannerService.lists();
    }
}

