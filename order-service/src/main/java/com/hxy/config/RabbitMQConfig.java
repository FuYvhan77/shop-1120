package com.hxy.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class RabbitMQConfig {

    @Value("${mqconfig.order_event_exchange}")
    private String eventExchange;
    @Value("${mqconfig.order_close_delay_queue}")
    private String orderCloseDelayQueue;
    @Value("${mqconfig.order_close_queue}")
    private String orderCloseQueue;
    @Value("${mqconfig.order_close_delay_routing_key}")
    private String orderCloseDelayRoutingKey;
    @Value("${mqconfig.order_close_routing_key}")
    private String orderCloseRoutingKey;
    @Value("${mqconfig.ttl}")
    private Integer ttl;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange(eventExchange, true, false);
    }

    // 延迟队列：设置了TTL、DLX和DLK，无消费者
    @Bean
    public Queue orderCloseDelayQueue() {
        Map<String, Object> args = new HashMap<>(3);
        args.put("x-dead-letter-exchange", eventExchange);
        args.put("x-dead-letter-routing-key", orderCloseRoutingKey);
        args.put("x-message-ttl", ttl);
        return new Queue(orderCloseDelayQueue, true, false, false, args);
    }

    // 消费队列：由监听器消费
    @Bean
    public Queue orderCloseQueue() {
        return new Queue(orderCloseQueue, true, false, false);
    }

    @Bean
    public Binding orderCloseDelayBinding() {
        return new Binding(orderCloseDelayQueue, Binding.DestinationType.QUEUE,
                eventExchange, orderCloseDelayRoutingKey, null);
    }

    @Bean
    public Binding orderCloseBinding() {
        return new Binding(orderCloseQueue, Binding.DestinationType.QUEUE,
                eventExchange, orderCloseRoutingKey, null);
    }
}