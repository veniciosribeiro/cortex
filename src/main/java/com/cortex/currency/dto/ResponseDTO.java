package com.cortex.currency.dto;

import com.cortex.currency.deserializer.LocalDateTimeDeserializer;
import com.cortex.currency.enums.Status;
import com.cortex.currency.serializer.LocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDTO extends BaseRequestDTO {
    private Status status;

    private String statusDescription;

    private boolean isCachedResult;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dataHoraConversao;

    public void setStatusDescription(Status status) {
        this.statusDescription = status.getStatus();
    }
}
