package com.hxy.pay;

import com.hxy.vo.PayInfoVO;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName WeixinPay
 * @date 2026-05-29 9:12
 */


public class WeixinPay implements PayInterface{
    @Override
    public String pay(PayInfoVO payInfoVO) {
        return "";
    }

    @Override
    public String queryPayState(String outTradeNo) {
        return "";
    }

    @Override
    public String refund(String outTradeNo) {
        return "";
    }
}
