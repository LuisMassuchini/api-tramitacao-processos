package com.camara.processos_api.controller;

import com.camara.processos_api.dto.EtapaRequestDTO;
import com.camara.processos_api.dto.EtapaResponseDTO;
import com.camara.processos_api.model.Usuario;
import com.camara.processos_api.service.EtapaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/processos") // O endpoint será aninhado sob processos
@RequiredArgsConstructor
public class EtapaController {

    private final EtapaService etapaService;

    // Endpoint unificado para criar etapa com ou sem arquivos
    @PostMapping(path = "/{processoId}/etapas", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<EtapaResponseDTO> adicionarEtapa(
            @PathVariable Long processoId,
            @Valid @RequestPart("etapa") EtapaRequestDTO etapaDTO,
            @RequestPart(value = "arquivos", required = false) List<MultipartFile> arquivos,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Usuario)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Usuario remetente = (Usuario) authentication.getPrincipal();
        EtapaResponseDTO novaEtapa = etapaService.criarEtapaComArquivos(processoId, etapaDTO, arquivos, remetente);
        return new ResponseEntity<>(novaEtapa, HttpStatus.CREATED);
    }
}