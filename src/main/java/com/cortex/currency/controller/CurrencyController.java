package com.cortex.currency.controller;

import com.cortex.currency.dto.CurrencyDTO;
import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.dto.ResponseDTO;
import com.cortex.currency.enums.Moeda;
import com.cortex.currency.service.ProducerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

    @ApiOperation(value = "Retorna o valor da conversão da Moeda Origem para a Moeda Destino")
    @GetMapping(value = {getPathConverter, getConverterWithPriority}, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<ResponseDTO> converte(
            @ApiParam(value = "Moeda que terá seu valor convertido para a Moeda Destino. Ex.: GBP", example = "GBP", defaultValue = "GBP") @PathVariable Moeda moedaOrigem,
            @ApiParam(value = "Moeda que apresentará o valor final convertido da Moeda Origem. Ex.: BRL", example = "BRL", defaultValue = "BRL") @PathVariable Moeda moedaDestino,
            @ApiParam(value = "Valor que será usado para conversão na Moeda Destino. Ex.: 1.5", example = "1.5") @PathVariable Double valorDesejado,
            @ApiParam(value = "Data da Cotação no Banco Central. Ex.: dd-MM-yyyy", example = "21-10-2020") @PathVariable LocalDate dataCotacao,
            @ApiParam(value = "Indica se tem prioridade de conversão sobre os outros pedidos (não está em uso)", example = "false", defaultValue = "false", readOnly = true, allowableValues = "false") @PathVariable(required = false) boolean priority
    ) {

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
