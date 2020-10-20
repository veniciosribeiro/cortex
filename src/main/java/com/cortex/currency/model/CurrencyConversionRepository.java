package com.cortex.currency.model;

import com.cortex.currency.entity.CurrencyConversion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface CurrencyConversionRepository extends JpaRepository<CurrencyConversion, Long> {

    CurrencyConversion findFirstByDataCotacaoAndMoedaOrigemAndMoedaFinalAndValorDesejadoOrderByIdDesc(LocalDate dataCotacao,
                                                                                                      String moedaOrigem,
                                                                                                      String moedaFinal,
                                                                                                      Double valorDesejado);
}
