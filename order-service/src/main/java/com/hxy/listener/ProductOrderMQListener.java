package com.hxy.listener;

import com.hxy.model.OrderMessage;
import com.hxy.service.ProductOrderService;
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
@RabbitListener(queues = "${mqconfig.order_close_queue}")
public class ProductOrderMQListener {

    @Autowired
    private ProductOrderService productOrderService;

    @RabbitHandler
    public void closeProductOrder(OrderMessage orderMessage,
                                  Message message, Channel channel) throws IOException {
        log.info("监听到关单消息: {}", orderMessage);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            boolean success = productOrderService.closeProductOrder(orderMessage);
            if (success) {
                channel.basicAck(deliveryTag, false);
            } else {
                log.warn("关单失败，重新入队: {}", orderMessage);
                channel.basicReject(deliveryTag, true);
            }
        } catch (Exception e) {
            log.error("关单异常: {}", orderMessage, e);
            channel.basicReject(deliveryTag, true);
        }
    }
}