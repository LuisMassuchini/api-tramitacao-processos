package com.camara.processos_api.dto;

import lombok.Data;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String matricula;
    private String nome;
    private String departamento;
    private String perfil;
}