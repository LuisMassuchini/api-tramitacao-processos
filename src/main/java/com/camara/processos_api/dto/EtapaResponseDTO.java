package com.camara.processos_api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EtapaResponseDTO {
    private Long id;
    private String deUsuarioNome;
    private String paraUsuarioNome;
    private Long paraUsuarioId;
    private String deDepartamento;
    private String paraDepartamento;
    private String status;
    private String observacao;
    private String assinatura;
    private LocalDateTime dataEnvio;
}