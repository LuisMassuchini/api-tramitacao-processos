package com.camara.processos_api.service;

import com.camara.processos_api.model.Notificacao;
import com.camara.processos_api.model.Usuario;
import com.camara.processos_api.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public void criarNotificacao(Usuario solicitante, Usuario destinatario, String demanda, String link) {
        Notificacao notificacao = new Notificacao();

        // Dados que já tínhamos
        notificacao.setSolicitante(solicitante.getNome());
        notificacao.setUsuarioDestino(destinatario);
        notificacao.setDemanda(demanda);
        notificacao.setLink(link);
        notificacao.setData(LocalDateTime.now());
        notificacao.setLida(false);

        // Garante perfil não nulo
        String perfil = destinatario.getPerfil();
        notificacao.setPerfil(perfil != null && !perfil.isBlank() ? perfil : "USER");

        // Define um valor padrão para 'excluir'
        notificacao.setExcluir(false);

        notificacaoRepository.save(notificacao);
    }
}