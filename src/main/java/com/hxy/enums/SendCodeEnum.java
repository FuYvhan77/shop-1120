package com.hxy.enums;

import lombok.Getter;

public enum SendCodeEnum {
    REGISTER("register", "用户注册"),
    LOGIN("login", "用户登录"),
    CHANGE_MOBILE("changeMobile", "修改手机号"),
    FORGET_PWD("forgetPwd", "忘记密码"),
    MODIFY_MOBILE("modifyMobile", "修改手机号"),
    MODIFY_EMAIL("modifyEmail", "修改邮箱"),
    MODIFY_PWD("modifyPwd", "修改密码"),
    MODIFY_USER_INFO("modifyUserInfo", "修改用户信息"),

    SEND_CODE_ERROR("sendCodeError", "发送验证码错误"),
    SEND_CODE_SUCCESS("sendCodeSuccess", "发送验证码成功"),
    CHECK_CODE_ERROR("checkCodeError", "验证码错误");

    @Getter
    private String code;
    @Getter
    private String message;

    SendCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
