package com.hxy.listener;

import com.hxy.model.CouponRecordMessage;
import com.hxy.service.CouponRecordService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.coupon_release_queue}")
public class CouponMQListener {

    @Autowired
    private CouponRecordService couponRecordService;

    @RabbitHandler
    public void releaseCouponRecord(CouponRecordMessage recordMessage,
                                    Message message, Channel channel) throws IOException {
        log.info("监听到消息：releaseCouponRecord 内容：{}", recordMessage);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        boolean success = couponRecordService.releaseCouponRecord(recordMessage);

        try {
            if (success) {
                // 确认消费，消息从队列移除
                channel.basicAck(deliveryTag, false);
            } else {
                // 业务要求重试（如订单状态仍为NEW），消息重新入队
                log.warn("释放优惠券失败，重新入队: {}", recordMessage);
                channel.basicReject(deliveryTag, true);
            }
        } catch (IOException e) {
            log.error("释放优惠券异常: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, true);
        }
    }
}