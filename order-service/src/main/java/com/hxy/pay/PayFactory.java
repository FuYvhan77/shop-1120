package com.hxy.pay;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName PayFactory
 * @date 2026-05-29 9:12
 */


public class PayFactory {
    public static PayInterface getPay(String payType) {
        if ("wx".equalsIgnoreCase(payType)) {
            return new WeixinPay();
        } else if ("ali".equalsIgnoreCase(payType)) {
            return new AibabaPay();
        }
        return null;
    }
}
