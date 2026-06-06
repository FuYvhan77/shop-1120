package com.hxy.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hxy.model.LoginUser;
import com.hxy.utils.CommonUtil;
import com.hxy.utils.JWTUtil;
import com.hxy.utils.JsonData;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName LoginInterceptor
 * @date 2026-05-15 9:28
 */


@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 1. 从Header中获取Token，若为空则尝试从URL参数获取
            String accessToken = request.getHeader("token");
            if (accessToken == null) {
                accessToken = request.getParameter("token");
            }

            if (StringUtils.isNotBlank(accessToken)) {
                // 2. 解密JWT
                Claims claims = JWTUtil.checkJWT(accessToken);
                if (claims == null) {
                    // Token无效或已过期
                    CommonUtil.sendJsonMessage(response,
                            JsonData.buildError("登录过期，重新登录"));
                    return false;
                }

                // 3. 提取Payload中的用户信息
                Long id = Long.valueOf(claims.get("id").toString());
                String headImg = (String) claims.get("head_img");
                String mail = (String) claims.get("mail");
                String name = (String) claims.get("name");

                // 4. 传递用户信息
                LoginUser loginUser = new LoginUser();
                loginUser.setId(id);
                loginUser.setName(name);
                loginUser.setMail(mail);
                loginUser.setHeadImg(headImg);
                //
                LoginInterceptor.threadLocal.set(loginUser);

                return true;
            }
        } catch (Exception e) {
            log.error("拦截器错误: {}", e.getMessage(), e);
        }

        CommonUtil.sendJsonMessage(response, JsonData.buildError("token不存在，重新登录"));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // 请求处理完成
        // 清空ThreadLocal
        threadLocal.remove();
    }
}
