package com.cortex.currency.dto;

import com.cortex.currency.deserializer.LocalDateDeserializer;
import com.cortex.currency.enums.Moeda;
import com.cortex.currency.serializer.DoubleSerializer;
import com.cortex.currency.serializer.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyDTO {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dataCotacao;

    private Moeda moedaOrigem;

    private Moeda moedaDestino;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double valorDesejado;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double valorConvertido;
}
