package com.cortex.currency.service.impl;

import com.cortex.currency.dto.CotacaoDTO;
import com.cortex.currency.dto.CotacaoPTAXDTO;
import com.cortex.currency.dto.CurrencyDTO;
import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.entity.CurrencyConversion;
import com.cortex.currency.enums.Moeda;
import com.cortex.currency.enums.Status;
import com.cortex.currency.exception.BancoCentralException;
import com.cortex.currency.model.CurrencyConversionRepository;
import com.cortex.currency.service.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class ConsumerServiceImpl implements ConsumerService {

    @Value("${api.bc.url}")
    private String apiBcUrl;

    @Value("${api.bc.conexao.timeout}")
    private Integer apiBcTimeout;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CurrencyConversionRepository currencyConversionRepository;

    private CurrencyConversion currencyConversion;

    public void processar(RequestDTO requestDTO) {

        try {
            CurrencyDTO currencyDTO = requestDTO.getCurrency();

            currencyConversion = Optional.ofNullable(
                    currencyConversionRepository.findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                            currencyDTO.getDataCotacao(),
                            currencyDTO.getMoedaOrigem().name(),
                            currencyDTO.getMoedaDestino().name(),
                            currencyDTO.getValorDesejado()
                    )
            ).orElseGet(() ->
                    CurrencyConversion.builder()
                            .priority(requestDTO.isPriority())
                            .dataCotacao(currencyDTO.getDataCotacao())
                            .moedaFinal(currencyDTO.getMoedaDestino().name())
                            .moedaOrigem(currencyDTO.getMoedaOrigem().name())
                            .valorDesejado(requestDTO.getCurrency().getValorDesejado())
                            .build()
            );

            CotacaoPTAXDTO cotacaoMoedaOrigem = getCotacaoBC(
                    requestDTO.getCurrency().getMoedaOrigem(),
                    requestDTO.getCurrency().getDataCotacao()
            );

            CotacaoPTAXDTO cotacaoMoedaDestino = getCotacaoBC(
                    requestDTO.getCurrency().getMoedaDestino(),
                    requestDTO.getCurrency().getDataCotacao()
            );

            Double valorConvertido = converter(cotacaoMoedaOrigem, cotacaoMoedaDestino);

            log.info("Cotação da moeda Origem {} ({} - Tipo: {}) {}",
                    cotacaoMoedaOrigem.getMoeda(),
                    cotacaoMoedaOrigem.getMoeda().getNomeFormatado(),
                    cotacaoMoedaOrigem.getMoeda().getTipoMoeda(),
                    cotacaoMoedaOrigem.getCotacaoDTO()
            );

            log.info("Cotação da moeda Destino {} ({} - Tipo: {}) {}",
                    cotacaoMoedaDestino.getMoeda(),
                    cotacaoMoedaDestino.getMoeda().getNomeFormatado(),
                    cotacaoMoedaDestino.getMoeda().getTipoMoeda(),
                    cotacaoMoedaDestino.getCotacaoDTO()
            );

            log.info("Conversão da moeda {} ({} - Tipo: {}) para a moeda {} ({} - Tipo: {} - Valor: {})",
                    cotacaoMoedaOrigem.getMoeda(),
                    cotacaoMoedaOrigem.getMoeda().getNomeFormatado(),
                    cotacaoMoedaOrigem.getMoeda().getTipoMoeda(),
                    cotacaoMoedaDestino.getMoeda(),
                    cotacaoMoedaDestino.getMoeda().getNomeFormatado(),
                    cotacaoMoedaDestino.getMoeda().getTipoMoeda(),
                    valorConvertido * requestDTO.getCurrency().getValorDesejado()
            );

            currencyConversion.setStatus(Status.CONVERTIDO);
            currencyConversion.setDataHoraConversao(LocalDateTime.now());
            currencyConversion.setValorConvertido(valorConvertido * requestDTO.getCurrency().getValorDesejado());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            currencyConversionRepository.save(currencyConversion);
        }
    }

    public CotacaoPTAXDTO getCotacaoBC(Moeda moeda, LocalDate dataCotacao) {

        log.info("Obtendo cotação da moeda {} ({})", moeda, moeda.getNomeFormatado());

        if (moeda.equals(Moeda.BRL)) {
            return getBRLPTAX();
        }

        String queryString = apiBcUrl + "?@moeda='" + moeda.name() + "'" +
                "&@dataCotacao='" + dataCotacao.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")) + "'" +
                "&$orderby=dataHoraCotacao desc" +
                "&$top=1" +
                "&$format=json";

        CotacaoPTAXDTO cotacaoPTAXDTO;

        try {
            cotacaoPTAXDTO = restTemplate.getForObject(queryString, CotacaoPTAXDTO.class);
        } catch (Exception e) {
            currencyConversion.setStatus(Status.ERRO_BANCO_CENTRAL);
            throw new BancoCentralException(Status.ERRO_BANCO_CENTRAL.getStatus(), e);
        }

        if (Optional.ofNullable(cotacaoPTAXDTO).isPresent()) {
            cotacaoPTAXDTO.setMoeda(moeda);

            if (Objects.isNull(cotacaoPTAXDTO.getCotacaoDTO().getCotacaoCompra())) {
                currencyConversion.setStatus(Status.ERRO_BANCO_CENTRAL_EMPTY);
                log.error("Moeda {} - Cotação {}", moeda, cotacaoPTAXDTO.getCotacaoDTO());
                throw new BancoCentralException(Status.ERRO_BANCO_CENTRAL_EMPTY.getStatus());
            }
        }

        return cotacaoPTAXDTO;
    }

    private Double converter(CotacaoPTAXDTO origem, CotacaoPTAXDTO destino) {
        return origem.getCotacaoDTO().getCotacaoCompra() / destino.getCotacaoDTO().getCotacaoCompra();
    }

    private CotacaoPTAXDTO getBRLPTAX() {
        CotacaoDTO cotacaoDTO = CotacaoDTO.builder()
                .cotacaoCompra(1d)
                .cotacaoVenda(1d)
                .paridadeCompra(1d)
                .paridadeVenda(1d)
                .build();

        return CotacaoPTAXDTO.builder()
                .moeda(Moeda.BRL)
                .cotacaoDTO(cotacaoDTO)
                .build();
    }
}
