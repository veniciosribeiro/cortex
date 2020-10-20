package com.cortex.currency.service;

import com.cortex.currency.dto.RequestDTO;

public interface ConsumerService {

    /**
     * Realiza o consumo da fila e processa as conversões solicitadas
     *
     * @param requestDTO
     */
    void processar(RequestDTO requestDTO);
}
