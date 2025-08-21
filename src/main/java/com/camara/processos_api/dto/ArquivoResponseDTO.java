package com.camara.processos_api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArquivoResponseDTO {
    private Long id;
    private String nomeArquivo;
    private String tipo;
    private LocalDateTime dataEnvio;
    private String enviadoPorNome; // Campo para o nome de quem enviou
    private Long idProcesso;
    private Long idEtapa;
    private Long paraUsuarioId;

    // Getters/Setters explícitos para MapStruct
    public String getEnviadoPorNome() { return enviadoPorNome; }
    public void setEnviadoPorNome(String enviadoPorNome) { this.enviadoPorNome = enviadoPorNome; }

    public Long getIdProcesso() { return idProcesso; }
    public void setIdProcesso(Long idProcesso) { this.idProcesso = idProcesso; }

    public Long getIdEtapa() { return idEtapa; }
    public void setIdEtapa(Long idEtapa) { this.idEtapa = idEtapa; }
    public Long getParaUsuarioId() { return paraUsuarioId; }
}
