package com.camara.processos_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProcessoRequestDTO {
    @NotBlank
    @Size(min = 5, max = 255)
    private String titulo;

    @NotBlank
    private String descricao;

    // Imagem da assinatura (Base64) capturada no formulário de criação
    private String assinaturaImagemBase64;

    // Identifica o usuário destinatário da primeira etapa do processo
    // Use Long para alinhar com UsuarioRepository.findById(Long)

}