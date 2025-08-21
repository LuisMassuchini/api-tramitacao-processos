package com.camara.processos_api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_destino")
    private Usuario usuarioDestino;

    @Column(name = "demanda", length = 1000) // Aumentar o tamanho se necessário
    private String demanda;

    private String link;

    @Column(name = "data")
    private LocalDateTime data;

    private boolean lida = false;

    @Column(name = "solicitante") // Adicione esta anotação e o campo
    private String solicitante;

    @Column(nullable = false)
    private Boolean excluir = false;

    @Column(nullable = false)
    private String perfil;


    // Campos como 'solicitante', 'tipo', etc. podem ser adicionados se necessário.
}
