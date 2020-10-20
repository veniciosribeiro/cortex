package com.cortex.currency;

import com.cortex.currency.dto.CurrencyDTO;
import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.enums.Moeda;
import com.cortex.currency.exception.RabbitException;
import com.cortex.currency.service.impl.QueueServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class QueueServiceTest {

    @InjectMocks
    private QueueServiceImpl queueService;

    @Mock
    private static QueueServiceImpl queueServiceMock;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private static CurrencyDTO currencyDTO;

    private static RequestDTO requestDTO;

    @BeforeAll
    public static void init() {
        currencyDTO = CurrencyDTO
                .builder()
                .moedaOrigem(Moeda.USD)
                .moedaDestino(Moeda.AUD)
                .valorDesejado(100d)
                .dataCotacao(LocalDate.now())
                .build();

        requestDTO = RequestDTO
                .builder()
                .currency(currencyDTO)
                .priority(true)
                .build();
    }

    @Test
    public void sendToQueueSuccessTest() {
        queueService.send(requestDTO);
    }

    @Test
    public void sendToQueueFailTest() {
        Mockito.doThrow(RabbitException.class).when(rabbitTemplate).convertAndSend(Mockito.any(), Mockito.any(), Mockito.anyString());
        Assertions.assertThrows(RabbitException.class, () -> queueService.send(requestDTO));

        Mockito.doThrow(AmqpException.class).when(rabbitTemplate).convertAndSend(Mockito.any(), Mockito.any(), Mockito.anyString());
        Assertions.assertThrows(RabbitException.class, () -> queueService.send(requestDTO));
    }
}