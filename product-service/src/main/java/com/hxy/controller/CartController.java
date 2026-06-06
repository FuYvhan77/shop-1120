package com.hxy.controller;

import com.hxy.request.CartItemRequest;
import com.hxy.service.CartService;
import com.hxy.utils.JsonData;
import com.hxy.vo.CartItemVO;
import com.hxy.vo.CartVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "购物车")
@RestController
@RequestMapping("/api/cart/v1")
public class CartController {

    @Autowired
    private CartService cartService;

    @ApiOperation("添加到购物车")
    @PostMapping("add")
    public JsonData addToCart(@RequestBody CartItemRequest cartItemRequest) {
        cartService.addToCart(cartItemRequest);
        return JsonData.buildSuccess();
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public JsonData cleanMyCart() {
        cartService.clear();
        return JsonData.buildSuccess();
    }


    @ApiOperation("查看我的购物车")
    @GetMapping("/mycart")
    public JsonData findMyCart() {
        CartVO cartVO = cartService.getMyCart();
        return JsonData.buildSuccess(cartVO);
    }


    @ApiOperation("删除购物项")
    @DeleteMapping("/delete/{product_id}")
    public JsonData deleteItem(@PathVariable("product_id") long productId) {
        cartService.deleteItem(productId);
        return JsonData.buildSuccess();
    }


    @ApiOperation("修改购物车数量")
    @PostMapping("change")
    public JsonData changeItemNum(@RequestBody CartItemRequest cartItemRequest) {
        cartService.changeItemNum(cartItemRequest);
        return JsonData.buildSuccess();
    }


    @PostMapping("/confirm_order_cart_items")
    public JsonData confirmOrderCartItem(@RequestBody List<Long> productIdList) {
        List<CartItemVO> cartItemVOList = cartService.confirmOrderCartItem(productIdList);
        return JsonData.buildSuccess(cartItemVOList);
    }
}