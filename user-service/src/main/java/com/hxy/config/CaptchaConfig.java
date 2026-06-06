package com.hxy.config;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class CaptchaConfig {

    @Bean
    @Qualifier("captchaProducer")
    public DefaultKaptcha kaptcha() {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 验证码长度
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        // 字符间隔
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "8");
        // 不添加噪声干扰线，提升用户体验
        properties.setProperty(Constants.KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.DefaultNoise");
        // 水波纹样式，防止OCR识别
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.WaterRipple");
        // 只使用数字，方便手机输入
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, "0123456789");
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}