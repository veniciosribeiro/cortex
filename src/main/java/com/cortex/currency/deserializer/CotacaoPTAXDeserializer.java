package com.cortex.currency.deserializer;

import com.cortex.currency.dto.CotacaoDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CotacaoPTAXDeserializer extends StdDeserializer<CotacaoDTO> {

    protected CotacaoPTAXDeserializer(Class<CotacaoDTO> vc) {
        super(vc);
    }

    public CotacaoPTAXDeserializer() {
        this(CotacaoDTO.class);
    }

    @Override
    public CotacaoDTO deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode rootNode = codec.readTree(parser);
        JsonNode indexNode = rootNode.get(0);

        CotacaoDTO.CotacaoDTOBuilder cotacaoDTO = CotacaoDTO.builder();

        return Objects.nonNull(indexNode)
                ? cotacaoDTO
                .cotacaoCompra(indexNode.get("cotacaoCompra").asDouble())
                .cotacaoVenda(indexNode.get("cotacaoVenda").asDouble())
                .paridadeCompra(indexNode.get("paridadeCompra").asDouble())
                .paridadeVenda(indexNode.get("paridadeVenda").asDouble())
                .dataHoraCotacao(this.parseDate(indexNode.get("dataHoraCotacao").asText()))
                .tipoBoletim(indexNode.get("tipoBoletim").asText())
                .build()
                : cotacaoDTO.build();
    }

    private LocalDate parseDate(String dataHoraCotacao) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.parse(dataHoraCotacao, formatter).toLocalDate();
    }
}
