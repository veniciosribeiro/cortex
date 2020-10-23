package com.cortex.currency.entity;

import com.cortex.currency.deserializer.LocalDateDeserializer;
import com.cortex.currency.deserializer.LocalDateTimeDeserializer;
import com.cortex.currency.enums.Status;
import com.cortex.currency.serializer.LocalDateSerializer;
import com.cortex.currency.serializer.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Status status;

    private boolean priority;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dataCotacao;

    private String moedaOrigem;

    private String moedaFinal;

    private Double valorDesejado;

    private Double valorConvertido;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dataHoraConversao;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dataHoraSolicitacao;
}
