package com.hxy.feign;

import com.hxy.utils.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface ProductOrderFeignSerivce {
    @GetMapping("/api/order/v1/query_state")
    JsonData queryProductOrderState(@RequestParam("out_trade_no") String outTradeNo);
}