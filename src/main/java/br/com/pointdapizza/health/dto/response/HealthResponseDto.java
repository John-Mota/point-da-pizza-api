package br.com.pointdapizza.health.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

public record HealthResponseDto(
    String status,
    boolean online,
    String servico,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String banco,
    OffsetDateTime timestamp
) {
}
