package com.cortex.currency.dto;

import com.cortex.currency.deserializer.CotacaoPTAXDeserializer;
import com.cortex.currency.enums.Moeda;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CotacaoPTAXDTO {

    private Moeda moeda;

    @JsonProperty(value = "value")
    @JsonDeserialize(using = CotacaoPTAXDeserializer.class)
    private CotacaoDTO cotacaoDTO;
}
