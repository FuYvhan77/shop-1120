package com.hxy.listener;

import com.hxy.model.ProductMessage;
import com.hxy.service.ProductService;
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
@RabbitListener(queues = "${mqconfig.stock_release_queue}")
public class ProductStockMQListener {

    @Autowired
    private ProductService productService;

    @RabbitHandler
    public void releaseProductStock(ProductMessage productMessage,
                                    Message message, Channel channel) throws IOException {
        log.info("监听到消息：releaseProductStock 内容：{}", productMessage);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        boolean success = productService.releaseProductStock(productMessage);
        try {
            if (success) {
                channel.basicAck(deliveryTag, false);
            } else {
                log.warn("释放商品库存失败，重新入队: {}", productMessage);
                channel.basicReject(deliveryTag, true);
            }
        } catch (IOException e) {
            log.error("释放商品库存异常: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, true);
        }
    }
}