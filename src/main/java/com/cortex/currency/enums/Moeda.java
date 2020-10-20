package com.cortex.currency.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Moeda {
    BRL("Real Brasileiro", TipoMoeda.A.name()),
    DKK("Coroa Dinamarquesa", TipoMoeda.A.name()),
    SEK("Coroa Sueca", TipoMoeda.A.name()),
    USD("Dólar Americano", TipoMoeda.A.name()),
    AUD("Dólar Australiano", TipoMoeda.B.name()),
    CAD("Dólar Canadense", TipoMoeda.A.name()),
    EUR("Euro", TipoMoeda.B.name()),
    JPY("Iene", TipoMoeda.A.name()),
    GBP("Libra Esterlina", TipoMoeda.B.name());

    private final String nomeFormatado;
    private final String tipoMoeda;

    Moeda(String nomeFormatado, String tipoMoeda) {
        this.nomeFormatado = nomeFormatado;
        this.tipoMoeda = tipoMoeda;
    }

    public static Moeda getEnumByString(String code) {
        return Arrays.stream(Moeda.values()).filter(m -> m.name().equals(code)).findFirst().orElse(null);
    }
}
