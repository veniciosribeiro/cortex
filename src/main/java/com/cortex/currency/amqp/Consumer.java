package com.cortex.currency.amqp;

import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.service.ConsumerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Consumer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private ConsumerService consumerService;

    @RabbitListener(queues = "${spring.rabbitmq.config.routing-key}")
    public void processar(Message message) {
        String json = new String(message.getBody());
        try {
            consumerService.processar(OBJECT_MAPPER.readValue(json, RequestDTO.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
