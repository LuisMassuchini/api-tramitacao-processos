package com.camara.processos_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProtocoloUpdateRequestDTO {

    @NotBlank(message = "O número do protocolo é obrigatório.")
    private String protocolo;

    // A ação também envolve encaminhar para o próximo responsável
    @NotNull(message = "É obrigatório definir um novo destinatário.")
    private Long proximoDestinatarioId;

    @NotBlank(message = "O status da nova etapa é obrigatório.")
    private String statusNovaEtapa;

    private String observacaoNovaEtapa;
}