package com.camara.processos_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "arquivos_processo")
public class Arquivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_processo", nullable = false)
    @JsonIgnore
    private Processo processo;

    @Column(name = "nome_arquivo")
    private String nomeArquivo;

    @Column(name = "caminho_arquivo")
    private String caminhoArquivo;

    private String tipo;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @ManyToOne
    @JoinColumn(name = "enviado_por")
    private Usuario enviadoPor;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etapa") // Pode ser nulo
    @JsonIgnore
    private Etapa etapa;

    // Getters explícitos para MapStruct
    public Long getId() { return id; }
    public Processo getProcesso() { return processo; }
    public String getNomeArquivo() { return nomeArquivo; }
    public String getCaminhoArquivo() { return caminhoArquivo; }
    public String getTipo() { return tipo; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public Usuario getEnviadoPor() { return enviadoPor; }
    public Etapa getEtapa() { return etapa; }
}
