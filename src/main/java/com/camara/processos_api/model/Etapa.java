package com.camara.processos_api.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "etapas_processo")
public class Etapa {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_processo")
    @JsonIgnore
    private Processo processo;

    @ManyToOne
    @JoinColumn(name = "de_usuario")
    private Usuario deUsuario;

    @ManyToOne
    @JoinColumn(name = "para_usuario")
    private Usuario paraUsuario;

    @Column(name = "de_departamento")
    private String deDepartamento;

    @Column(name = "para_departamento")
    private String paraDepartamento;

    private String status;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String observacao;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String assinatura;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    // Getters explícitos para MapStruct
    public Long getId() { return id; }
    public Processo getProcesso() { return processo; }
    public Usuario getDeUsuario() { return deUsuario; }
    public Usuario getParaUsuario() { return paraUsuario; }
    public String getDeDepartamento() { return deDepartamento; }
    public String getParaDepartamento() { return paraDepartamento; }
    public String getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public String getAssinatura() { return assinatura; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
}
