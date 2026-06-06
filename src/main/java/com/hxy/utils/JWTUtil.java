package com.hxy.utils;

import com.hxy.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class JWTUtil {

    // Token过期时间：正常7天，测试环境改70天方便调试
    private static final long EXPIRE = 1000 * 60 * 60 * 24 * 7 * 10;

    // 签名密钥（生产环境应通过配置中心下发，且定期轮换）
    private static final String SECRET = "hxyweifuwu2ban";

    // Token前缀，方便网关或前端识别
    private static final String TOKEN_PREFIX = "1024shop";

    // 令牌主题（issuer）
    private static final String SUBJECT = "hxy";

    /**
     * 根据用户信息生成JWT Token
     */
    public static String geneJsonWebToken(LoginUser loginUser) {
        if (loginUser == null) {
            throw new NullPointerException("loginUser对象为空");
        }

        String token = Jwts.builder()
                .setSubject(SUBJECT)
                // 自定义claims，存放用户信息
                .claim("head_img", loginUser.getHeadImg())
                .claim("id", loginUser.getId())
                .claim("name", loginUser.getName())
                .claim("mail", loginUser.getMail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();

        return TOKEN_PREFIX + token;
    }

    /**
     * 校验Token并返回Claims（解析失败返回null）
     */
    public static Claims checkJWT(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();
        } catch (Exception e) {
            log.warn("JWT Token解密失败: {}", e.getMessage());
            return null;
        }
    }
}