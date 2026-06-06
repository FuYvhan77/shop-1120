package com.hxy.controller;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hxy.config.AlipayConfig;
import com.hxy.enums.ClientType;
import com.hxy.enums.ProductOrderPayTypeEnum;
import com.hxy.interceptor.LoginInterceptor;
import com.hxy.model.LoginUser;
import com.hxy.request.ConfirmOrderRequest;
import com.hxy.request.RepayOrderRequest;
import com.hxy.service.ProductOrderService;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.CommonUtil;
import com.hxy.utils.JsonData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zyd
 * @since 2026-05-21
 */
@Api(tags = "订单服务")
@RestController
@RequestMapping("/api/order/v1")
@Slf4j
public class ProductOrderController {

    @Autowired
    private ProductOrderService productOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @ApiOperation("提交订单-支付")
    @PostMapping("confirm")
    public void confirm(@RequestBody ConfirmOrderRequest request, HttpServletResponse response) throws IOException {
        String result = productOrderService.confirmOrder(request);
        // 将支付宝返回的HTML表单直接输出给浏览器
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(result);
        response.getWriter().flush();
    }


    @ApiOperation("查询订单状态")
    @GetMapping("query_state")
    public JsonData queryProductOrderState(
            @ApiParam("订单号") @RequestParam("out_trade_no") String outTradeNo) {

        String state = productOrderService.queryProductOrderState(outTradeNo);
        log.info("查询订单状态: {}", state);
        return StringUtils.isBlank(state)
                ? JsonData.buildResult(BizCodeEnum.ORDER_CONFIRM_NOT_EXIST)
                : JsonData.buildSuccess(state);
    }


    @GetMapping("test_pay")
    public void testAlipay(HttpServletResponse response) throws Exception {
        // 构建支付业务参数
        Map<String, String> content = new HashMap<>();
        content.put("out_trade_no", UUID.randomUUID().toString());  // 商户订单号
        content.put("product_code", "FAST_INSTANT_TRADE_PAY");      // 销售产品码
        content.put("total_amount", "111.99");                      // 订单金额（元）
        content.put("subject", "杯子");                             // 商品标题
        content.put("body", "好的杯子");                            // 商品描述
        content.put("timeout_express", "5m");                       // 支付超时时间

        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizContent(JSON.toJSONString(content));
        request.setNotifyUrl("wwww.baidu.com");  // 异步通知地址
        request.setReturnUrl("www.mi.com"); // 同步跳转地址

        // 发起支付请求，获取支付表单HTML
        AlipayTradeWapPayResponse alipayResponse = AlipayConfig.getInstance().pageExecute(request);

        if (alipayResponse.isSuccess()) {
            // 将支付宝返回的HTML表单直接输出给浏览器
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(alipayResponse.getBody());
            response.getWriter().flush();
        } else {
            log.error("支付宝下单失败: {}", alipayResponse.getMsg());
        }
    }


    @PostMapping("/alipay")
    @ResponseBody  // 返回字符串到响应体
    public String alipayCallback(HttpServletRequest request) {
        // 1. 将支付宝POST的参数全部提取到Map中
        Map<String, String> paramsMap = convertRequestParamsToMap(request);
        log.info("支付宝回调通知: {}", paramsMap);

        try {
            // 2. 验签：用支付宝公钥验证参数签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    paramsMap,
                    AlipayConfig.ALIPAY_PUB_KEY,
                    AlipayConfig.CHARSET,
                    AlipayConfig.SIGN_TYPE
            );

            if (signVerified) {
                // 3. 验签通过，处理订单状态
                JsonData result = productOrderService.handlerOrderCallbackMsg(
                        ProductOrderPayTypeEnum.ALIPAY, paramsMap);
                if (result.getCode() == 0) {
                    return "success";  // 通知支付宝已确认，停止重试
                }
            } else {
                log.error("支付宝回调验签失败: {}", paramsMap);
            }
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常: {}", e.getMessage(), e);
        }

        return "failure";  // 通知支付宝重试
    }

    /**
     * 将HttpServletRequest中的参数转换为Map<String, String>
     * 支付宝通知参数均为单值，这里做了简单处理
     */
    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
        Map<String, String> paramsMap = new HashMap<>(16);
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            // 支付宝通知参数均为单值，取第一个即可
            paramsMap.put(name, values.length > 0 ? values[0] : "");
        }
        return paramsMap;
    }


    @ApiOperation("分页查询我的订单列表")
    @GetMapping("page")
    public JsonData pageOrderList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String state) {
        return JsonData.buildSuccess(productOrderService.page1(page, size, state));
    }


    @ApiOperation("重新支付订单")
    @PostMapping("/repay")
    public void repay(@RequestBody RepayOrderRequest repayRequest, HttpServletResponse response) throws IOException {
        java.lang.String pay = productOrderService.repay(repayRequest);
        if (pay== null){
            response.setContentType("text/html;charset=UTF-8");
            JsonData jsonData = JsonData.buildResult(BizCodeEnum.ORDER_CONFIRM_NOT_EXIST);
            response.getWriter().write(JSON.toJSONString(jsonData));
            response.getWriter().flush();
        }else {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(pay);
            response.getWriter().flush();
        }
    }



    @ApiOperation("获取提交订单令牌")
    @GetMapping("get_token")
    public JsonData getOrderToken() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String token = CommonUtil.getRandomString(32);
        redisTemplate.opsForValue().set("user:order:"+loginUser.getId(), token, 30, TimeUnit.MINUTES);
        return JsonData.buildSuccess(token);
    }
}

