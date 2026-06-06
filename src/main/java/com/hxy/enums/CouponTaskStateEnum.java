package com.hxy.enums;

public enum CouponTaskStateEnum {
    LOCK,    // 已锁定，资源被占用
    FINISH,  // 已完成，资源被正式消费
    CANCEL   // 已取消，资源已释放
}