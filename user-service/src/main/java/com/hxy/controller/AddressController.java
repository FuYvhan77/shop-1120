package com.hxy.controller;


import com.hxy.model.AddressDO;
import com.hxy.request.AddressAddRequest;
import com.hxy.service.AddressService;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.JsonData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 电商-公司收发货地址表 前端控制器
 * </p>
 *
 * @author zyd
 * @since 2026-05-13
 */
@RestController
@RequestMapping("/api/address/v1")
@Api(tags = "地址接口")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping("/add")
    @ApiOperation("添加地址")
    public JsonData add(@RequestBody AddressAddRequest address) {
        return addressService.add(address);
    }



    @ApiOperation("查询地址")
    @GetMapping("/find/{id}")
    public JsonData find(@PathVariable("id") Long id) {
        return addressService.find(id);
    }

    @ApiOperation("删除地址")
    @DeleteMapping("/del/{id}")
    public JsonData del(@PathVariable("id") Long id) {
        return addressService.del(id);
    }

    @ApiOperation("查询地址列表")
    @GetMapping("/list")
    public JsonData list() {
        return addressService.listaddr();

    }

}

