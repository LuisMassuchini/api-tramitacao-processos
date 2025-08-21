package com.camara.processos_api.dto; // Verifique seu pacote

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // Importe a List

@Data
public class ProcessoResponseDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private String status;
    private String departamentoOrigem;
    private LocalDateTime dataCriacao;
    private String nomeCriador;
    // Novo: número do protocolo
    private String protocolo;

    private List<EtapaResponseDTO> etapas;
    private List<ArquivoResponseDTO> arquivos;
}