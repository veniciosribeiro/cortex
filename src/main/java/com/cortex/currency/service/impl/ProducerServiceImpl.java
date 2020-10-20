package com.cortex.currency.service.impl;

import com.cortex.currency.dto.CurrencyDTO;
import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.dto.ResponseDTO;
import com.cortex.currency.entity.CurrencyConversion;
import com.cortex.currency.enums.Status;
import com.cortex.currency.exception.GenericException;
import com.cortex.currency.model.CurrencyConversionRepository;
import com.cortex.currency.service.ProducerService;
import com.cortex.currency.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@Slf4j
public class ProducerServiceImpl implements ProducerService {

    @Value("${spring.rabbitmq.config.exchange}")
    public String exchange;

    @Value("${spring.rabbitmq.config.routing-key}")
    public String routingKey;

    @Value("${api.cache.timeout}")
    public int cacheTime;

    @Autowired
    private CurrencyConversionRepository currencyConversionRepository;

    @Autowired
    private QueueService queueService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ResponseDTO sendToBaseThenQueue(RequestDTO requestDTO) throws GenericException {

        CurrencyDTO currencyDTO = requestDTO.getCurrency();

        CurrencyConversion currencyConversion = getCurrencyConversionFromCache(currencyDTO);

        ResponseDTO.ResponseDTOBuilder<?, ?> responseDTO = ResponseDTO.builder();

        if (isValidCache(currencyConversion)) {
            currencyDTO.setValorConvertido(currencyConversion.getValorConvertido());
            responseDTO.isCachedResult(true);
        } else {
            currencyConversion = CurrencyConversion
                    .builder()
                    .dataCotacao(currencyDTO.getDataCotacao())
                    .valorDesejado(currencyDTO.getValorDesejado())
                    .moedaFinal(currencyDTO.getMoedaDestino().name())
                    .moedaOrigem(currencyDTO.getMoedaOrigem().name())
                    .priority(requestDTO.isPriority())
                    .status(Status.AG_CONVERSAO)
                    .build();

            responseDTO.isCachedResult(false);

            currencyConversionRepository.save(currencyConversion);

            this.queueService.send(requestDTO);
        }

        return responseDTO
                .priority(currencyConversion.isPriority())
                .status(currencyConversion.getStatus())
                .statusDescription(currencyConversion.getStatus().getStatus())
                .dataHoraConversao(currencyConversion.getDataHoraConversao())
                .currency(currencyDTO)
                .build();
    }

    private CurrencyConversion getCurrencyConversionFromCache(CurrencyDTO currencyDTO) {
        return currencyConversionRepository
                .findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(
                        currencyDTO.getDataCotacao(), currencyDTO.getMoedaOrigem().name(),
                        currencyDTO.getMoedaDestino().name(), currencyDTO.getValorDesejado()
                );
    }

    private boolean isValidCache(CurrencyConversion currencyConversion) {
        if (Objects.nonNull(currencyConversion)) {
            log.info("Resultado retornado do cache");

            if (Objects.nonNull(currencyConversion.getValorConvertido())) {
                log.info("Conversão da moeda {} para a moeda {} - Valor: {}",
                        currencyConversion.getMoedaOrigem(),
                        currencyConversion.getMoedaFinal(),
                        currencyConversion.getValorConvertido()
                );
            }

            if (Objects.nonNull(currencyConversion.getDataHoraConversao())) {
                LocalDateTime dataHoraConversao = currencyConversion.getDataHoraConversao();
                LocalDateTime currentTime = LocalDateTime.now();

                LocalDateTime tempDateTime = LocalDateTime.from(dataHoraConversao);

                long elapsedTime = tempDateTime.until(currentTime, ChronoUnit.SECONDS);

                log.info("Segundos para renovar o cache: {}", (cacheTime * 60) - elapsedTime);

                return elapsedTime <= (cacheTime * 60);
            }
            return true;
        }
        return false;
    }
}
