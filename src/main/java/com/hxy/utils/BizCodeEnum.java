package com.hxy.utils;

import lombok.Getter;

public enum BizCodeEnum {
    // 通用操作码
    OPS_REPEAT(110001, "重复操作"),

    // 验证码
    CODE_TO_ERROR(240001, "接收号码不合规"),
    CODE_LIMITED(240002, "验证码发送过快"),
    CODE_ERROR(240003, "验证码错误"),
    CODE_CAPTCHA(240101, "图形验证码错误"),

    // 账号
    ACCOUNT_REPEAT(250001, "账号已经存在"),
    ACCOUNT_UNREGISTER(250002, "账号不存在"),
    ACCOUNT_PWD_ERROR(250003, "账号或者密码错误"),

    //地址
    USER_ADDRESS_NOT_EXIST(260001, "用户地址不存在"),


    //优惠卷
    COUPON_NOT_ENOUGH_PRICE(270001, "优惠券价格不足"),
    COUPON_NOT_EXIST_USER(270001, "优惠卷超领"),

    COUPON_NOT_ENOUGH(270002, "优惠券数量不足"),
    COUPON_EXPIRED(270003, "优惠券已过期"),
    COUPON_NOT_USABLE(270004, "优惠券不可用"),
    COUPON_NOT_FOUND(270005, "优惠券不存在"),
    COUPON_USED(270006, "优惠券已使用"),
    COUPON_NOT_PUBLISH(270007, "优惠券未发布"),
    COUPON_NOT_EXIST(270001, "优惠券不存在"),
    PRODUCT_NOT_EXIST(270008, "商品不存在"),
    CART_FAIL(270009, "商品不在购物车中"),
    COUPON_RECORD_LOCK_FAIL(270010, "优惠券锁定失败"),
    ORDER_CONFIRM_NOT_EXIST(270011, "订单确认信息不存在"),
    ORDER_CONFIRM_LOCK_PRODUCT_FAIL(270012, "订单确认锁定商品失败"),
    ADDRESS_NO_EXITS(270013, "地址不存在"),
    ORDER_CONFIRM_CART_ITEM_NOT_EXIST(270014, "订单确认商品不存在"),
    ORDER_CONFIRM_COUPON_FAIL(270015, "订单确认优惠券失败"),
    COUPON_UNAVAILABLE(270016, "优惠券不可用"),
    ORDER_CONFIRM_PRICE_FAIL(270017, "订单验价失败"),
    ORDER_CONFIRM_TOKEN_NOT_EXIST(270018, "订单确认令牌不存在"),
    ORDER_CONFIRM_TOKEN_EQUAL_FAIL(270019, "订单确认令牌不一致")
    ;
    @Getter
    private String message;
    @Getter
    private int code;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
