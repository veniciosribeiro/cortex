package com.cortex.currency.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Status {
    AG_CONVERSAO("Aguardando conversão"),
    CONVERTIDO("Convertido"),
    ERRO_AO_CONVERTER("Erro ao converter"),
    ERRO_BANCO_CENTRAL("Erro ao obter cotação"),
    ERRO_BANCO_CENTRAL_EMPTY("Cotação sem resultados");

    private final String status;

    Status(String status) {
        this.status = status;
    }

    public static Status getEnumByString(String code) {
        return Arrays.stream(Status.values()).filter(m -> m.name().equals(code)).findFirst().orElse(null);
    }
}
