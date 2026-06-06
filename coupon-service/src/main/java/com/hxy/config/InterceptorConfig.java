package com.hxy.config;

import com.hxy.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/api/coupon_record/*/**")  // 我的优惠券需要登录
                .addPathPatterns("/api/coupon/*/**")         // 领券需要登录
                // 列表接口放行
                .excludePathPatterns("/api/coupon/*/list",
                        "/api/coupon_record/*/new_user_coupon"
                ,"/api/coupon/*/get_coupon",
                        "/api/coupon/*/get_coupon2",
                        "/api/coupon/*/updata",
                "/api/coupon/*/updata_coupon");
    }
}