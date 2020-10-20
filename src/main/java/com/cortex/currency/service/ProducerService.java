package com.cortex.currency.service;

import com.cortex.currency.dto.RequestDTO;
import com.cortex.currency.dto.ResponseDTO;
import com.cortex.currency.enums.Status;
import com.cortex.currency.exception.GenericException;

public interface ProducerService {

    /**
     * Salva a requisição de conversão no banco de dados e envia
     * para a fila do RabbitMQ
     *
     * @param currency
     * @return Retorna os dados iniciais de solicitação.
     * Caso tenha sido convertido com cesso, retorna o Status CONVERTIDO.
     * Caso não tenha sido convertido, retorna um dos Status abaixo:
     * ERRO_AO_CONVERTER("Erro ao converter")
     * ERRO_BANCO_CENTRAL("Erro ao obter cotação")
     * ERRO_BANCO_CENTRAL_EMPTY("Cotação sem resultados")
     * <p>
     * See {@link Status}
     * @throws GenericException
     */
    ResponseDTO sendToBaseThenQueue(RequestDTO currency) throws GenericException;

}
