package com.cortex.currency.service;

import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.exception.RabbitException;

public interface QueueService {

    /**
     * Envia o pedido de conversão para a fila
     *
     * @param requestDTO
     * @throws RabbitException
     */
    void send(RequestDTO requestDTO) throws RabbitException;
}
