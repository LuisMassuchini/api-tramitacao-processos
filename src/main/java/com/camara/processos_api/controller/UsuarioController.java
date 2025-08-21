package com.camara.processos_api.controller;

import com.camara.processos_api.dto.UsuarioResponseDTO;
import com.camara.processos_api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Usuários", description = "Endpoints para consulta de usuários")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Lista todos os usuários",
            description = "Retorna uma lista de todos os usuários para popular seletores no frontend.")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos(
            @RequestParam(name = "apenasResponsaveis", defaultValue = "false") boolean apenasResponsaveis
    ) {
        List<UsuarioResponseDTO> lista = usuarioService.listarTodos(apenasResponsaveis);
        System.out.println("[DEBUG] /api/usuarios chamado. apenasResponsaveis=" + apenasResponsaveis + ", retornando " + lista.size() + " usuários.");
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Lista apenas usuários responsáveis",
            description = "Retorna somente usuários com perfis autorizados (admin, secadm, secleg, secjur).")
    @GetMapping("/responsaveis")
    public ResponseEntity<List<UsuarioResponseDTO>> listarResponsaveis() {
        List<UsuarioResponseDTO> lista = usuarioService.listarTodos(true);
        System.out.println("[DEBUG] /api/usuarios/responsaveis chamado. Retornando " + lista.size() + " usuários.");
        return ResponseEntity.ok(lista);
    }
}