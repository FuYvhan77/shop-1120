package com.hxy.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;

public class AlipayConfig {
    // 支付宝网关地址（沙箱环境）
    public static final String PAY_GATEWAY = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    // 应用APPID
    public static final String APPID = "9021000138649086";

    // 应用私钥（商户自己保管，绝对保密）
    public static final String APP_PRI_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC6AIDgqx077gxGBIMJYV7Jt9flgqSTYw4IZYT1YqIP5VprrzYbSUK6AoxWdofPzvN31XjflVqv//pIcjeGkookSl0Y/9updWs0WJ48vgC0jXXKo1JVIiEA3vUYCPZRMBQ/i8tBNuiHV4GZ30WwBE0NsCXTBpPsLfLkNWgJmUqTDNWgJBNTVjtRky/yWdCBeajoUmH+7wRqAlE86l7kQutHA9Vbiytbkk3D2rvlpc92/z+L2TdqzcLyQkYKLdMzExOoafhqF0lvM0A6r7XmLmj8qex/ThagHaA3ZRmIhFOUUiY4NwpumfEcSVQuSBM667gUpdJY0WGgBYaURbbB9+MVAgMBAAECggEBAIb4+IyP/z3O5z8j5Qa4jrUoYFWesNh3J+a17H64nUuVrkC4OzYfunkKglX322PUFF7gHL96PP28bh2GLH7N9cqR3+7VM8xLdYjdya3gXtimUlTCoWs/wd1E7WwMjNmrXvYh9lOTibc0q8pB9+rbpCKLsVbpzguz6C7sBlv4zeHYUIKQ+fZQjhpvvfgy804V6+ynZoSF7znGMbG+y2Bkk9bPYOBj6Ma4K901qsaJdX280FP5QB8KeHQzM7wdC9jq8XrE9ZpwAxdGEYXa40RsAJG6DPYA+GapSYww/xvdvvSaEfevzMC8JGXb+rumdMgupghBk2pUgWbJvogKeLSrtoECgYEA4inbr3vDsdrsR3/8AiYSu/BXlTbrbyN6Arf1PkO7YocyQpmIrePI9doCZ/WCK+MMAEN0ER3nWBBgRjUd6dktAgXPTE7NhJBEFxPOaZZyzz3jC4fxXtGKCNZ3N/HQ+H5eaq/QIgkb0eZ5nIg9ZB6hJL6woyO2oPVvPh/YpNV4JvUCgYEA0opIjzYiDn/UsvjiDEGRWABdW0qoRbt3JKUteIZ50+kE4WZIyTbcvDqwjYkCmm+/bhFVpADCcK5/l8LBda5gHweXsYfRMor2ttauhPtu8OyyLsnHeMS4zvCEqGZ4Nq21ulmQzSqSHRxehoqWXDKpKim3hLNzHGAMM5zoA5ft96ECgYBikaG4mrVQx7xg8SvRd+yEUpypYNU9i/W6R5iwOW7q40eJdUb3mqZUWF7iFkNUg1EQsFVevGy+meyLzMyrxcZy8jtF47Z4xzOV50D4DeUd47gXp3kQPTzu2yH8lPzhwJm/375ThyrD0bJBAbPX9e1/iUiM4ZzQDAgeyFxwgsUqHQKBgEE+Uw/2MYWWvX4cvEgaBYVkz4lb0FuTpW84tKeV3Kj7m+SRgtq/4pV+BnpFzAW5vXPJcWWaSqcKnEWWN8dVGzpiavK76OslvyCX1IsnM5D7eJghNWxKYg7W3/Ujk/s1x4wQ91ts5syYnv5IciHuHRgQx2sbBP/1D7Lq28dJTJghAoGBAK3Er5xO0YWEScEH/DsOU00Xp2OYJYpm/+VQTk5EPKM/y+iqmbHj/BvUCzP13E74XjMBb9pxTbbVAvlrexXg8cFEOsgJn+mXFS0TqaylE2Xb4DTTY8Ye7aAvdCrdUb/KFsqRafg2R9l6QC3YajjAqV1iXacmHZbCKxV4Vq3ZqRvD";

    // 支付宝公钥（从支付宝开放平台下载）
    public static final String ALIPAY_PUB_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzsaJAuOJ2wfZRLOXT8RUQ/E7cpb5+GIA2hLdcuoZN3rTXEuob02lmoMC8vMjIDusv5OJu7jQSDq8iEka0lZhqN5EGxF7p79w4sw0cgEV8Osoe9Iyau8RT5RTrHfOFvop02XrtA4KSUfRHaGZYTvwZVX2AM+RabGZDoRIXx6GqxdV285vYLIln3F5JG+maetAOOZxl9alx988Wq8HaEM6yJCVL2wEiwH8angiTXXHpoWzeoLjokmLN65PTdkYIhzesLXhHqU2RAV5ynlI9kQVFyHrItkmnpkmqUMuNoG2O1p7AhpspysN/HE3bFWYkVlkJhplAMJfpljyN/4rW8OyHQIDAQAB";

    // 签名算法
    public static final String SIGN_TYPE = "RSA2";

    // 字符编码
    public static final String CHARSET = "UTF-8";

    // 返回格式
    public static final String FORMAT = "json";

    private AlipayConfig() {}


    private volatile static AlipayClient instance = null;
    /**
     * 单例模式获取, 双重锁校验
     * @return
     */
    public static AlipayClient getInstance(){

        if(instance==null){
            synchronized (AlipayConfig.class){
                if(instance == null){
                    instance = new DefaultAlipayClient(PAY_GATEWAY,APPID,APP_PRI_KEY,FORMAT,CHARSET,ALIPAY_PUB_KEY,SIGN_TYPE);
                }
            }
        }
        return instance;
    }
}