package com.hxy.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hxy.enums.SendCodeEnum;
import com.hxy.service.NotifyService;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.CommonUtil;
import com.hxy.utils.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName NotifyServiceImpl
 * @date 2026-05-14 9:41
 */
@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${spring.mail.from}")
    private String from;

    // 发送邮件验证码
    @Override
    public JsonData sendCode(SendCodeEnum sendCodeEnum, String to) {
        //效验是否在60秒内发送重复发送
        String string = redisTemplate.opsForValue().get("user-service:code:" + to);
        if (StringUtils.isNotBlank(string)){
            //发送过
            String[] split = string.split("-");
            if (System.currentTimeMillis() - Long.parseLong(split[1]) < 60000){
                //60秒内发送过
                return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
            }
        }



        //1, 生成验证码
        String code = CommonUtil.getRandomString(4);

        //2, 验证邮箱是否正确
        if (!CommonUtil.isEmail(to)){
            return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("验证码");

        //2, 判断什么发送验证码
        if (sendCodeEnum == SendCodeEnum.REGISTER){
            message.setText("当前验证码用于注册，您的验证码是：" + code+",请妥善保存");
        }else if (sendCodeEnum == SendCodeEnum.LOGIN){
            message.setText("当前验证码用于登录，您的验证码是：" + code+",请妥善保存");
        }else if (sendCodeEnum == SendCodeEnum.CHANGE_MOBILE){

        }else {
            return JsonData.buildResult(BizCodeEnum.OPS_REPEAT);
        }

        javaMailSender.send(message);
        log.info("邮箱验证码：{}",code);
        //TODO 验证码存入redis，设置有效期10分钟
        redisTemplate.opsForValue().set("user-service:code:"+to,code+"-"+System.currentTimeMillis(),10*60, TimeUnit.SECONDS);
        return JsonData.buildSuccess();
    }
}
