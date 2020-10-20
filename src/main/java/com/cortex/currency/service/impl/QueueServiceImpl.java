package com.cortex.currency.service.impl;

import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.exception.RabbitException;
import com.cortex.currency.service.QueueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QueueServiceImpl implements QueueService {

    @Value("${spring.rabbitmq.config.exchange}")
    public String exchange;

    @Value("${spring.rabbitmq.config.routing-key}")
    public String routingKey;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(RequestDTO requestDTO) throws RabbitException {
        log.info("Adicionando item a fila para conversão {}", requestDTO);
        try {
            String json = new ObjectMapper().writeValueAsString(requestDTO);
            rabbitTemplate.convertAndSend(exchange, routingKey, json);
            log.info("Item adicionado a fila com sucesso {}", requestDTO);
        } catch (AmqpException | JsonProcessingException e) {
            log.error("Erro ao adicionar item a fila {}", requestDTO);
            throw new RabbitException("Erro ao adicionar item a fila", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
