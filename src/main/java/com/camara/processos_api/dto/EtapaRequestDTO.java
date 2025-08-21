package com.camara.processos_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EtapaRequestDTO {


    @NotNull(message = "O ID do usuário de destino é obrigatório.")
    private Long paraUsuarioId;

    private String protocolo;

    private String assinaturaImagemBase64;

    // NOVO: indica que deve usar assinatura de texto padronizada
    private boolean usarAssinaturaTexto;

    @NotBlank(message = "O departamento de destino é obrigatório.")
    private String paraDepartamento;

    @NotBlank(message = "O status da etapa é obrigatório.")
    private String status;

    private String observacao;
    private String assinatura;
}