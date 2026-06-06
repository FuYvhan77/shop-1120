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

    @Value("${mqconfig.coupon_event_exchange}")
    private String eventExchange;
    @Value("${mqconfig.coupon_release_delay_queue}")
    private String couponReleaseDelayQueue;
    @Value("${mqconfig.coupon_release_delay_routing_key}")
    private String couponReleaseDelayRoutingKey;
    @Value("${mqconfig.coupon_release_queue}")
    private String couponReleaseQueue;
    @Value("${mqconfig.coupon_release_routing_key}")
    private String couponReleaseRoutingKey;
    @Value("${mqconfig.ttl}")
    private Integer ttl;

    // 消息转换器：将Java对象自动序列化为JSON
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Topic类型交换机，持久化，非自动删除
    @Bean
    public Exchange couponEventExchange() {
        return new TopicExchange(eventExchange, true, false);
    }

    // 延迟队列——核心：TTL + DLX
    @Bean
    public Queue couponReleaseDelayQueue() {
        Map<String, Object> args = new HashMap<>(3);
        args.put("x-message-ttl", ttl);
        args.put("x-dead-letter-exchange", eventExchange);
        args.put("x-dead-letter-routing-key", couponReleaseRoutingKey);
        return new Queue(couponReleaseDelayQueue, true, false, false, args);
    }

    // 实际消费队列
    @Bean
    public Queue couponReleaseQueue() {
        return new Queue(couponReleaseQueue, true, false, false);
    }

    // 绑定：交换机 → 延迟队列
    @Bean
    public Binding couponReleaseDelayBinding() {
        return new Binding(couponReleaseDelayQueue, Binding.DestinationType.QUEUE,
                eventExchange, couponReleaseDelayRoutingKey, null);
    }

    // 绑定：交换机 → 消费队列
    @Bean
    public Binding couponReleaseBinding() {
        return new Binding(couponReleaseQueue, Binding.DestinationType.QUEUE,
                eventExchange, couponReleaseRoutingKey, null);
    }
}