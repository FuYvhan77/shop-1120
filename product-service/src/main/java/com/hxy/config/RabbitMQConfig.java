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

    @Value("${mqconfig.stock_event_exchange}")
    private String eventExchange;
    @Value("${mqconfig.stock_release_delay_queue}")
    private String stockReleaseDelayQueue;
    @Value("${mqconfig.stock_release_delay_routing_key}")
    private String stockReleaseDelayRoutingKey;
    @Value("${mqconfig.stock_release_queue}")
    private String stockReleaseQueue;
    @Value("${mqconfig.stock_release_routing_key}")
    private String stockReleaseRoutingKey;
    @Value("${mqconfig.ttl}")
    private Integer ttl;

    // 消息转换器：将Java对象自动序列化为JSON
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Topic类型交换机，持久化，非自动删除
    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange(eventExchange, true, false);
    }

    // 延迟队列——核心：TTL + DLX
    @Bean
    public Queue stockReleaseDelayQueue() {
        Map<String, Object> args = new HashMap<>(3);
        args.put("x-message-ttl", ttl);
        args.put("x-dead-letter-exchange", eventExchange);
        args.put("x-dead-letter-routing-key", stockReleaseRoutingKey);
        return new Queue(stockReleaseDelayQueue, true, false, false, args);
    }

    // 实际消费队列
    @Bean
    public Queue stockReleaseQueue() {
        return new Queue(stockReleaseQueue, true, false, false);
    }

    // 绑定：交换机 → 延迟队列
    @Bean
    public Binding stockReleaseDelayBinding() {
        return new Binding(stockReleaseDelayQueue, Binding.DestinationType.QUEUE,
                eventExchange, stockReleaseDelayRoutingKey, null);
    }

    // 绑定：交换机 → 消费队列
    @Bean
    public Binding stockReleaseBinding() {
        return new Binding(stockReleaseQueue, Binding.DestinationType.QUEUE,
                eventExchange, stockReleaseRoutingKey, null);
    }
}