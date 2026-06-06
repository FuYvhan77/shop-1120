package com.hxy.pay;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.hxy.config.AlipayConfig;
import com.hxy.vo.PayInfoVO;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
public class AibabaPay implements PayInterface {
    @Override
    public String pay(PayInfoVO payInfoVO) {
        try {

            // 构建支付业务参数
            Map<String, String> content = new HashMap<>();
            content.put("out_trade_no", payInfoVO.getOutTradeNo());  // 商户订单号
            content.put("product_code", "FAST_INSTANT_TRADE_PAY");      // 销售产品码
            content.put("total_amount", payInfoVO.getPayFee().toString());                      // 订单金额（元）
            content.put("subject", payInfoVO.getTitle());                             // 商品标题
            content.put("body", payInfoVO.getDescription());                            // 商品描述
            content.put("timeout_express", payInfoVO.getOrderPayTimeoutMills() + "s");                       // 支付超时时间

            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            request.setBizContent(JSON.toJSONString(content));
            //TODO 设置回调地址
            request.setNotifyUrl("http://m6a8cbaf.natappfree.cc/api/order/v1/alipay");  // 异步通知地址
            request.setReturnUrl("www.mi.com"); // 同步跳转地址

            // 发起支付请求，获取支付表单HTML
            AlipayTradeWapPayResponse alipayResponse = AlipayConfig.getInstance().pageExecute(request);

            if (alipayResponse.isSuccess()) {
                // 将支付宝返回的HTML表单直接输出给浏览器

                String body = alipayResponse.getBody();
                return body;
            } else {
                log.error("支付宝下单失败: {}", alipayResponse.getMsg());
            }
        } catch (Exception e) {
            log.error("支付宝下单失败: {}", e);
            return "error";
        }
        return "error";
    }

    @Override
    public String queryPayState(String outTradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, String> content = new HashMap<>();
        content.put("out_trade_no", outTradeNo);
        request.setBizContent(JSON.toJSONString(content));

        try {
            AlipayTradeQueryResponse response = AlipayConfig.getInstance().execute(request);
            log.info("支付宝查单响应: {}", response.getBody());

            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                // TRADE_SUCCESS表示支付成功，WAIT_BUYER_PAY表示等待付款
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    return tradeStatus; // 返回非空，代表已支付
                }
            }
        } catch (AlipayApiException e) {
            log.error("支付宝查单异常: payInfo={}", e);
        }
        return null; // 返回空，代表未支付或查询失败
    }

    @Override
    public String refund(String outTradeNo) {
        return "";
    }
}
