package com.hxy.config;

import com.hxy.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                // 需要登录才能访问的路径
                .addPathPatterns("/api/user/*/**", "/api/address/*/**")
                // 无需登录即可访问的路径（白名单）
                .excludePathPatterns(
                    "/api/user/*/send_code",
                    "/api/user/*/captcha",
                    "/api/user/*/register",
                    "/api/user/*/login",
                    "/api/user/*/upload"
                );
    }
}