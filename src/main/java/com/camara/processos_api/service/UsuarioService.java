package com.camara.processos_api.service;

import com.camara.processos_api.dto.UsuarioResponseDTO;
import com.camara.processos_api.mapper.UsuarioMapper;
import com.camara.processos_api.model.Usuario;
import com.camara.processos_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos(boolean apenasResponsaveis) {
        List<Usuario> usuarios;

        if (apenasResponsaveis) {
            // ESTA LISTA É O CRITÉRIO:
            // Apenas usuários com um destes perfis serão retornados.
            List<String> perfisAutorizados = List.of("admin", "secadm", "secleg", "secjur");

            // O método findByPerfilIn busca no banco de dados: "SELECT * FROM usuario WHERE perfil IN ('admin', 'secadm', ...)"
            usuarios = usuarioRepository.findByPerfilInIgnoreCase(perfisAutorizados);
        } else {
            // Se o filtro não for solicitado, busca todos os usuários
            usuarios = usuarioRepository.findAll();
        }

        // Mapeia a lista de entidades para a lista de DTOs para a resposta
        return usuarios.stream()
                .map(usuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }}