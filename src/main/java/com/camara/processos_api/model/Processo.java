package com.camara.processos_api.model;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import jakarta.persistence.OrderBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "processos_tramitacao")
public class Processo {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Lob
    @Column(columnDefinition = "LONGTEXT") // garante tipo longo no MySQL
    private String descricao;
    private String status;

    @Column(name = "departamento_origem")
    private String departamentoOrigem;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "protocolo")
    private String protocolo;

    @ManyToOne
    @JoinColumn(name = "criado_por")
    private Usuario criadoPor;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    @Fetch(FetchMode.SUBSELECT)
    private List<Etapa> etapas = new ArrayList<>();

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<Arquivo> arquivos = new ArrayList<>();

    // Getters explícitos para MapStruct
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getStatus() { return status; }
    public String getDepartamentoOrigem() { return departamentoOrigem; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public String getProtocolo() { return protocolo; }
    public Usuario getCriadoPor() { return criadoPor; }
    public java.util.List<Etapa> getEtapas() { return etapas; }
    public java.util.List<Arquivo> getArquivos() { return arquivos; }
}
