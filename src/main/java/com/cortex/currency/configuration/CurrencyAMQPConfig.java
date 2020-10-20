package com.cortex.currency.configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CurrencyAMQPConfig {

    @Value("${spring.rabbitmq.config.exchange}")
    public String exchange;

    @Value("${spring.rabbitmq.config.routing-key}")
    public String routingKey;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue queue() {
        return QueueBuilder
                .durable(routingKey)
                .build();
    }

    @Bean
    Binding bindingQueue() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(routingKey);
    }
}
