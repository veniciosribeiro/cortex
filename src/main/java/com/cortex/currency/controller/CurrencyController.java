package com.cortex.currency.controller;

import com.cortex.currency.dto.CurrencyDTO;
import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.dto.ResponseDTO;
import com.cortex.currency.enums.Moeda;
import com.cortex.currency.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class CurrencyController {

    private static final String getPathConverter = "/converte/{moedaOrigem}/{moedaDestino}/{valorDesejado}/{dataCotacao}";
    private static final String getConverterWithPriority = getPathConverter + "/{priority}";

    @Autowired
    private ProducerService producerService;

    @GetMapping(value = {getPathConverter, getConverterWithPriority}, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<ResponseDTO> converte(@PathVariable Moeda moedaOrigem, @PathVariable Moeda moedaDestino, @PathVariable Double valorDesejado,
                                         @PathVariable LocalDate dataCotacao, @PathVariable(required = false) boolean priority) {

        CurrencyDTO currency = CurrencyDTO
                .builder()
                .moedaOrigem(moedaOrigem)
                .moedaDestino(moedaDestino)
                .valorDesejado(valorDesejado)
                .dataCotacao(dataCotacao)
                .build();

        RequestDTO requestDTO = RequestDTO
                .builder()
                .currency(currency)
                .priority(priority)
                .build();

        return new ResponseEntity<>(producerService.sendToBaseThenQueue(requestDTO), HttpStatus.OK);
    }

}
