package com.cortex.currency;

import com.cortex.currency.dto.CotacaoDTO;
import com.cortex.currency.dto.CotacaoPTAXDTO;
import com.cortex.currency.dto.CurrencyDTO;
import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.entity.CurrencyConversion;
import com.cortex.currency.enums.Moeda;
import com.cortex.currency.enums.Status;
import com.cortex.currency.exception.BancoCentralException;
import com.cortex.currency.model.CurrencyConversionRepository;
import com.cortex.currency.service.impl.ConsumerServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@TestPropertySource(properties = {
        "api.bc.url=null",
})
public class ConsumerServiceTest extends AbstractSpringBootTest {

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Mock
    private CurrencyConversionRepository currencyConversionRepository;

    @Mock
    RestTemplate restTemplate;

    @Value("${api.bc.url}")
    private static String apiBcUrl;

    private static CurrencyConversion currencyConversion;

    private static RequestDTO requestDTO;

    private static CurrencyDTO currency;

    private static CotacaoPTAXDTO cotacaoPTAXDTOOrigem;

    private static CotacaoPTAXDTO cotacaoPTAXDTODestino;

    private static String queryStringOrigem;
    private static String queryStringDestino;

    @BeforeEach
    void reset() {
        createDTOS();

        queryStringOrigem = apiBcUrl + "?@moeda='" + requestDTO.getCurrency().getMoedaOrigem().name() + "'" +
                "&@dataCotacao='" + currency.getDataCotacao().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")) + "'" +
                "&$orderby=dataHoraCotacao desc" +
                "&$top=1" +
                "&$format=json";

        queryStringDestino = apiBcUrl + "?@moeda='" + requestDTO.getCurrency().getMoedaDestino().name() + "'" +
                "&@dataCotacao='" + currency.getDataCotacao().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")) + "'" +
                "&$orderby=dataHoraCotacao desc" +
                "&$top=1" +
                "&$format=json";

        Mockito.when(restTemplate.getForObject(queryStringOrigem, CotacaoPTAXDTO.class))
                .thenReturn(cotacaoPTAXDTOOrigem);

        Mockito.when(restTemplate.getForObject(queryStringDestino, CotacaoPTAXDTO.class))
                .thenReturn(cotacaoPTAXDTODestino);
    }

    @Test
    public void processarItemTest() {
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(currencyConversion);

        consumerService.processar(requestDTO);

        Assertions.assertEquals(Status.CONVERTIDO, currencyConversion.getStatus());
        Assertions.assertNotNull(currencyConversion.getDataHoraConversao());
        Assertions.assertNotNull(currencyConversion.getValorConvertido());
        Assertions.assertEquals(cotacaoPTAXDTOOrigem.getMoeda().name(), currencyConversion.getMoedaOrigem());
        Assertions.assertEquals(cotacaoPTAXDTODestino.getMoeda().name(), currencyConversion.getMoedaFinal());
    }

    @Test
    public void processarItemBRLTest() {
        currency.setMoedaOrigem(Moeda.BRL);
        currencyConversion.setMoedaOrigem(currency.getMoedaOrigem().name());
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(currencyConversion);

        consumerService.processar(requestDTO);

        Assertions.assertEquals(Status.CONVERTIDO, currencyConversion.getStatus());
        Assertions.assertNotNull(currencyConversion.getDataHoraConversao());
        Assertions.assertNotNull(currencyConversion.getValorConvertido());
        Assertions.assertEquals(Moeda.BRL.name(), currencyConversion.getMoedaOrigem());
        Assertions.assertEquals(cotacaoPTAXDTODestino.getMoeda().name(), currencyConversion.getMoedaFinal());
    }

    @Test
    public void processarItemSemDBTest() {
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(null);

        consumerService.processar(requestDTO);

        Assertions.assertEquals(Status.AG_CONVERSAO, currencyConversion.getStatus());
        Assertions.assertNull(currencyConversion.getDataHoraConversao());
        Assertions.assertNull(currencyConversion.getValorConvertido());
        Assertions.assertEquals(cotacaoPTAXDTOOrigem.getMoeda().name(), currencyConversion.getMoedaOrigem());
        Assertions.assertEquals(cotacaoPTAXDTODestino.getMoeda().name(), currencyConversion.getMoedaFinal());
    }

    @Test
    public void processarItemFalha500BCTest() {
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(currencyConversion);

        Mockito.when(restTemplate.getForObject(queryStringOrigem, CotacaoPTAXDTO.class))
                .thenThrow(BancoCentralException.class);

        consumerService.processar(requestDTO);

        Assertions.assertEquals(Status.ERRO_BANCO_CENTRAL, currencyConversion.getStatus());
        Assertions.assertNotNull(currencyConversion.getDataHoraSolicitacao());
        Assertions.assertNull(currencyConversion.getDataHoraConversao());
        Assertions.assertNull(currencyConversion.getValorConvertido());
        Assertions.assertEquals(cotacaoPTAXDTOOrigem.getMoeda().name(), currencyConversion.getMoedaOrigem());
        Assertions.assertEquals(cotacaoPTAXDTODestino.getMoeda().name(), currencyConversion.getMoedaFinal());
    }

    @Test
    public void processarItemFalhaCotacaoBCTest() {
        cotacaoPTAXDTOOrigem.setCotacaoDTO(new CotacaoDTO());
        Mockito.when(
                currencyConversionRepository
                        .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                                currency.getDataCotacao(), currency.getMoedaOrigem().name(),
                                currency.getMoedaDestino().name(), currency.getValorDesejado()
                        )
        ).thenReturn(currencyConversion);

        Mockito.when(restTemplate.getForObject(queryStringOrigem, CotacaoPTAXDTO.class))
                .thenReturn(cotacaoPTAXDTOOrigem);

        consumerService.processar(requestDTO);

        Assertions.assertEquals(Status.ERRO_BANCO_CENTRAL_EMPTY, currencyConversion.getStatus());
        Assertions.assertNotNull(currencyConversion.getDataHoraSolicitacao());
        Assertions.assertNull(currencyConversion.getDataHoraConversao());
        Assertions.assertNull(currencyConversion.getValorConvertido());
        Assertions.assertEquals(cotacaoPTAXDTOOrigem.getMoeda().name(), currencyConversion.getMoedaOrigem());
        Assertions.assertEquals(cotacaoPTAXDTODestino.getMoeda().name(), currencyConversion.getMoedaFinal());
    }

    private static void createDTOS() {
        currency = CurrencyDTO
                .builder()
                .moedaOrigem(Moeda.USD)
                .moedaDestino(Moeda.AUD)
                .valorDesejado(100d)
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
                .dataHoraSolicitacao(LocalDateTime.now())
                .valorDesejado(currency.getValorDesejado())
                .moedaFinal(currency.getMoedaDestino().name())
                .moedaOrigem(currency.getMoedaOrigem().name())
                .priority(requestDTO.isPriority())
                .status(Status.AG_CONVERSAO)
                .id(1L)
                .build();

        cotacaoPTAXDTOOrigem = CotacaoPTAXDTO.builder()
                .moeda(currency.getMoedaOrigem())
                .cotacaoDTO(CotacaoDTO.builder()
                        .cotacaoCompra(3D)
                        .cotacaoVenda(3D)
                        .dataHoraCotacao(LocalDate.now())
                        .paridadeCompra(3D)
                        .paridadeVenda(3D)
                        .build())
                .build();

        cotacaoPTAXDTODestino = CotacaoPTAXDTO.builder()
                .moeda(currency.getMoedaDestino())
                .cotacaoDTO(CotacaoDTO.builder()
                        .cotacaoCompra(5D)
                        .cotacaoVenda(5D)
                        .dataHoraCotacao(LocalDate.now())
                        .paridadeCompra(5D)
                        .paridadeVenda(5D)
                        .build())
                .build();
    }
}