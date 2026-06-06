package com.hxy.pay;

import com.hxy.vo.PayInfoVO;

public interface PayInterface {
    // 支付
    String pay(PayInfoVO payInfoVO);
    // 查询支付状态
    String queryPayState(String outTradeNo);
    // 退款
    String refund(String outTradeNo);
}
