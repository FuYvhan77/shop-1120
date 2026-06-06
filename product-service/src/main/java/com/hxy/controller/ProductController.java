package com.hxy.controller;

import com.hxy.model.ProductDO;
import com.hxy.request.LockProductRequest;
import com.hxy.service.ProductService;
import com.hxy.utils.JsonData;
import com.hxy.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Api(tags = "商品模块")
@RestController
@RequestMapping("/api/product/v1")
public class ProductController {

    @Autowired
    private ProductService productService;

    @ApiOperation("分页查询商品列表")
    @GetMapping("/page")
    public JsonData pageProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return JsonData.buildSuccess(productService.page1(page, size));
    }

    @ApiOperation("商品详情")
    @GetMapping("/detail/{product_id}")
    public JsonData detail(@PathVariable("product_id") long productId) {
        ProductVO productVO = productService.findDetailById(productId);
        return JsonData.buildSuccess(productVO);
    }


    @ApiOperation("商品库存锁定")
    @PostMapping("lock_products")
    public JsonData lockProducts(@RequestBody LockProductRequest lockProductRequest) {
        return productService.lockProductStock(lockProductRequest);
    }



    @ApiOperation("新增商品")
    @PostMapping("/add")
    public JsonData add(@RequestBody ProductDO productDO) {
        return  productService.saveproductDO(productDO);

    }



    @ApiOperation("商品搜索")
    @GetMapping("/search")
    public JsonData searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return productService.searchProducts(keyword, minPrice, maxPrice);
    }


}