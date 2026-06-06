package com.hxy.controller;


import com.google.code.kaptcha.Producer;
import com.hxy.enums.SendCodeEnum;
import com.hxy.service.NotifyService;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.CommonUtil;
import com.hxy.utils.JsonData;
import com.hxy.utils.MinioUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Api(tags = "通知接口")
@RestController
@RequestMapping("/api/notify/v1")
@Slf4j
public class NotifyController {

    @Autowired
    private Producer producer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private NotifyService notifyService;

    private static final long CAPTCHA_CODE_EXPIRED = 60 * 1000 * 10; // 10分钟有效

    /**
     * 获取验证码
     * <p>生成图形验证码并输出到响应流中，验证码文本会记录到日志</p>
     *
     * @param response HTTP响应对象，用于输出验证码图片
     */
    @GetMapping("/captcha")
    @ApiOperation("获取验证码")
    public void captcha(HttpServletResponse response, HttpServletRequest request) {
        String text = producer.createText();
        log.info("验证码：{}", text);

        // 将验证码文本存入Redis，key由IP+UA的MD5组成    123:456
        String captchaKey = getCaptchaKey(request);
        redisTemplate.opsForValue().set(captchaKey, text, CAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);


        BufferedImage image = producer.createImage(text);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            ImageIO.write(image, "jpg", outputStream);
            outputStream.flush();
        } catch (Exception e) {
            log.error("验证码生成异常：{}", e.getMessage());
        }
    }


    private String getCaptchaKey(HttpServletRequest request) {
        String ipAddr = CommonUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        // 生成key 分文件夹 ：前面就是文件夹
        return "user-service:captcha:" + CommonUtil.MD5(ipAddr + userAgent);
    }


    @ApiOperation("发送验证码")
    @GetMapping("/send_code")
    public JsonData sendCode(@ApiParam("收信人") @RequestParam String to,
                             @ApiParam("图形验证码") @RequestParam String captcha,
                             HttpServletRequest request) {


        // 获取验证码key
        String captchaKey = getCaptchaKey(request);
        // 从redis获取获取验证码
        String vslue = redisTemplate.opsForValue().get(captchaKey);

        if (captcha != null && vslue != null && captcha.equalsIgnoreCase(vslue)) {
            //验证码正确
            redisTemplate.delete(captchaKey);
            //发送邮件
            return notifyService.sendCode(SendCodeEnum.REGISTER,to);
        } else {
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA);
        }

    }

}
