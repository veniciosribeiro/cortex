package com.cortex.currency;

import com.cortex.currency.dto.CurrencyDTO;
import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.dto.ResponseDTO;
import com.cortex.currency.entity.CurrencyConversion;
import com.cortex.currency.enums.Moeda;
import com.cortex.currency.enums.Status;
import com.cortex.currency.exception.RabbitException;
import com.cortex.currency.model.CurrencyConversionRepository;
import com.cortex.currency.service.impl.ProducerServiceImpl;
import com.cortex.currency.service.impl.QueueServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
public class ProducerServiceTest {

    @InjectMocks
    ProducerServiceImpl producerService;

    @Mock
    private static QueueServiceImpl queueService;

    @Mock
    CurrencyConversionRepository currencyConversionRepository;

    private static CurrencyConversion currencyConversion;

    private static RequestDTO requestDTO;

    private static CurrencyDTO currency;

    @BeforeAll
    public static void init() {

        currency = CurrencyDTO
                .builder()
                .moedaOrigem(Moeda.BRL)
                .moedaDestino(Moeda.AUD)
                .valorDesejado(100d)
                .dataCotacao(LocalDate.now())
                .build();

        requestDTO = RequestDTO
                .builder()
                .currency(currency)
                .priority(true)
                .build();
    }

    @BeforeEach
    void reset() {
        currencyConversion = CurrencyConversion
                .builder()
                .dataCotacao(currency.getDataCotacao())
                .valorDesejado(currency.getValorDesejado())
                .moedaFinal(currency.getMoedaDestino().name())
                .moedaOrigem(currency.getMoedaOrigem().name())
                .priority(requestDTO.isPriority())
                .status(Status.AG_CONVERSAO)
                .build();
    }

    @Test
    public void testSendToBaseThenQueueSuccess() {
        currencyConversion.setId(null);
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(null);

        currencyConversion.setId(1L);
        Mockito.when(currencyConversionRepository.save(currencyConversion)).thenReturn(currencyConversion);

        Mockito.doNothing().when(queueService).send(requestDTO);

        ResponseDTO responseDTO = producerService.sendToBaseThenQueue(requestDTO);

        Assertions.assertFalse(responseDTO.isCachedResult());
    }

    @Test
    public void testSendToBaseThenQueueFail() throws RabbitException {
        currencyConversion.setId(null);
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(null);

        currencyConversion.setId(1L);
        Mockito.when(currencyConversionRepository.save(currencyConversion)).thenReturn(currencyConversion);

        Mockito.doThrow(RabbitException.class).when(queueService).send(requestDTO);

        Assertions.assertThrows(RabbitException.class, () -> producerService.sendToBaseThenQueue(requestDTO));
    }

    @Test
    public void testConvertidoSucesso() {
        currencyConversion.setId(1L);
        currencyConversion.setValorConvertido(1D);
        currencyConversion.setDataHoraConversao(LocalDateTime.now());
        currencyConversion.setStatus(Status.CONVERTIDO);
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(currencyConversion);

        ResponseDTO responseDTO = producerService.sendToBaseThenQueue(requestDTO);

        Assertions.assertTrue(responseDTO.isCachedResult());
        Assertions.assertEquals(Status.CONVERTIDO, responseDTO.getStatus());
    }

    @Test
    public void testRenewCache() {
        currencyConversion.setId(1L);
        currencyConversion.setValorConvertido(1D);
        currencyConversion.setDataHoraConversao(LocalDateTime.now().minusMinutes(30));
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(currencyConversion);

        ResponseDTO responseDTO = producerService.sendToBaseThenQueue(requestDTO);

        Assertions.assertFalse(responseDTO.isCachedResult());
        Assertions.assertEquals(Status.AG_CONVERSAO, currencyConversion.getStatus());
    }

    @Test
    public void testValidCache() {
        currencyConversion.setId(1L);
        currencyConversion.setValorConvertido(1D);
        currencyConversion.setDataHoraConversao(LocalDateTime.now().minusMinutes(0));
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(currencyConversion);

        ResponseDTO responseDTO = producerService.sendToBaseThenQueue(requestDTO);

        Assertions.assertTrue(responseDTO.isCachedResult());
    }

    @Test
    public void testValidSemDataConversao() {
        currencyConversion.setId(1L);
        currencyConversion.setValorConvertido(null);
        currencyConversion.setDataHoraConversao(null);
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(currencyConversion);

        ResponseDTO responseDTO = producerService.sendToBaseThenQueue(requestDTO);

        Assertions.assertTrue(responseDTO.isCachedResult());
    }
}