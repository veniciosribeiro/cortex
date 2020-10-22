package com.cortex.currency;

import com.cortex.currency.controller.CurrencyController;
import com.cortex.currency.dto.CurrencyDTO;
import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.dto.RestResponseErrorDTO;
import com.cortex.currency.entity.CurrencyConversion;
import com.cortex.currency.enums.Moeda;
import com.cortex.currency.enums.Status;
import com.cortex.currency.exception.RabbitException;
import com.cortex.currency.model.CurrencyConversionRepository;
import com.cortex.currency.service.impl.QueueServiceImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebMvcTest(CurrencyController.class)
@SpringJUnitConfig
public class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private CurrencyConversionRepository currencyConversionRepository;

    @Mock
    private QueueServiceImpl queueService;

    private static CurrencyConversion currencyConversion;

    private static RequestDTO requestDTO;

    private static CurrencyDTO currency;

    private static ObjectMapper OBJECT_MAPPER;

    private static String urlRequest;

    @BeforeAll
    public static void init() {
        OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        currency = CurrencyDTO
                .builder()
                .moedaOrigem(Moeda.USD)
                .moedaDestino(Moeda.EUR)
                .valorDesejado(1D)
                .dataCotacao(LocalDate.now())
                .build();

        requestDTO = RequestDTO
                .builder()
                .currency(currency)
                .priority(true)
                .build();

        currencyConversion = CurrencyConversion
                .builder()
                .dataCotacao(currency.getDataCotacao())
                .valorDesejado(currency.getValorDesejado())
                .moedaFinal(currency.getMoedaDestino().name())
                .moedaOrigem(currency.getMoedaOrigem().name())
                .priority(requestDTO.isPriority())
                .status(Status.AG_CONVERSAO)
                .build();

        urlRequest = "/converte/" +
                requestDTO.getCurrency().getMoedaOrigem()
                + "/" + requestDTO.getCurrency().getMoedaDestino()
                + "/" + requestDTO.getCurrency().getValorDesejado()
                + "/" + requestDTO.getCurrency().getDataCotacao().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                + "/" + requestDTO.isPriority();
    }

    @Test
    void solicitarConversaoTest() throws Exception {
        currencyConversion.setId(1L);
        Mockito.when(currencyConversionRepository.save(currencyConversion)).thenReturn(currencyConversion);

        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.any(), Mockito.any(), Mockito.anyString());

        Mockito.doNothing().when(queueService).send(requestDTO);

        mockMvc.perform(MockMvcRequestBuilders.get(urlRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    Assertions.assertEquals(200, result.getResponse().getStatus());
                    Status status = Status.getEnumByString(JsonPath.read(result.getResponse().getContentAsString(), "$.status"));
                    Assertions.assertEquals(Status.AG_CONVERSAO, status);
                });
    }

    @Test
    void solicitarConversaoFromCacheTest() throws Exception {
        currencyConversion.setId(1L);
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(currencyConversion);

        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.any(), Mockito.any(), Mockito.anyString());
        Mockito.doNothing().when(queueService).send(requestDTO);

        mockMvc.perform(MockMvcRequestBuilders.get(urlRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    Assertions.assertEquals(200, result.getResponse().getStatus());
                    boolean cachedResult = JsonPath.read(result.getResponse().getContentAsString(), "$.cachedResult");
                    Assertions.assertTrue(cachedResult);
                });
    }

    @Test
    void erroAoAdicionarNoBancoTest() throws Exception {
        currencyConversion.setId(null);
        Mockito.when(currencyConversionRepository.save(currencyConversion)).thenThrow(new IllegalArgumentException("Erro ao adicionar no banco"));

        mockMvc.perform(MockMvcRequestBuilders.get(urlRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    Assertions.assertEquals(500, result.getResponse().getStatus());
                    Assertions.assertTrue(result.getResolvedException() instanceof IllegalArgumentException);
                    Assertions.assertEquals("Erro ao adicionar no banco", result.getResolvedException().getMessage());
                });
    }

    @Test
    void erroAoAdicionarNaFilaTest() throws Exception {
        currencyConversion.setId(1L);
        Mockito.when(currencyConversionRepository.save(currencyConversion)).thenReturn(currencyConversion);

        Mockito.doNothing().when(queueService).send(requestDTO);

        Mockito.doThrow(AmqpException.class)
                .when(rabbitTemplate).convertAndSend(Mockito.any(), Mockito.any(), Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.get(urlRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    RestResponseErrorDTO restResponseError = OBJECT_MAPPER.readValue(result.getResponse().getContentAsString(), RestResponseErrorDTO.class);

                    Assertions.assertEquals(500, restResponseError.getStatus());
                    Assertions.assertTrue(result.getResolvedException() instanceof RabbitException);
                    Assertions.assertEquals("Erro ao adicionar item a fila", result.getResolvedException().getMessage());
                });
    }

    @Test
    void validaErro400FormatoDataInvalidaTest() throws Exception {
        String uri = "/converte/" +
                requestDTO.getCurrency().getMoedaOrigem()
                + "/" + requestDTO.getCurrency().getMoedaDestino()
                + "/" + requestDTO.getCurrency().getValorDesejado()
                + "/" + requestDTO.getCurrency().getDataCotacao().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                + "/" + requestDTO.isPriority();

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        Assertions.assertEquals(400, mvcResult.getResponse().getStatus());
    }

}
