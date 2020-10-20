package com.cortex.currency.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class CotacaoDTO {
    private Double paridadeCompra;

    private Double paridadeVenda;

    private Double cotacaoCompra;

    private Double cotacaoVenda;

    private LocalDate dataHoraCotacao;

    private String tipoBoletim;
}
