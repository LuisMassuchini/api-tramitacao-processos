package com.camara.processos_api.controller;

import com.camara.processos_api.dto.ProcessoRequestDTO;
import com.camara.processos_api.dto.ProcessoResponseDTO;
import com.camara.processos_api.dto.ProtocoloUpdateRequestDTO;
import com.camara.processos_api.model.Usuario;
import com.camara.processos_api.service.ProcessoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

@Tag(name = "Processos", description = "Endpoints para gerenciamento de processos")
@RestController
@RequestMapping("/api/processos")
@RequiredArgsConstructor
public class ProcessoController {

    private final ProcessoService processoService;

    // --- Leitura ---
    @Operation(summary = "Busca um processo pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProcessoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(processoService.buscarPorId(id));
    }

    // --- Criação ---
    @PreAuthorize("hasAnyRole('ADMIN', 'SECADM', 'SECLEG', 'SECJUR')")
    @Operation(summary = "Cria um novo processo (sem anexo inicial)")
    @PostMapping
    public ResponseEntity<ProcessoResponseDTO> criarNovoProcesso(
            @Valid @RequestBody ProcessoRequestDTO processoDTO,
            Authentication authentication) {

        // Forma mais segura e direta de obter o usuário logado
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        ProcessoResponseDTO novoProcesso = processoService.criarProcesso(processoDTO, usuarioLogado);
        return new ResponseEntity<>(novoProcesso, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SECADM', 'SECLEG', 'SECJUR')")
    @Operation(summary = "Cria um novo processo com um anexo inicial")
    @PostMapping(path = "/com-arquivo", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProcessoResponseDTO> criarNovoProcessoComArquivo(
            @Valid @RequestPart("processo") ProcessoRequestDTO processoDTO,
            @RequestPart(value = "arquivos", required = false) List<MultipartFile> arquivos,
            Authentication authentication) {

        // A mesma abordagem segura aqui
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        ProcessoResponseDTO novoProcesso = processoService.criarProcessoComArquivo(processoDTO, arquivos, usuarioLogado);
        return new ResponseEntity<>(novoProcesso, HttpStatus.CREATED);
    }

    // --- Atualização ---
    @PreAuthorize("hasAnyRole('ADMIN', 'PROTOCOL')")
    @Operation(summary = "Atualiza um processo existente")
    @PutMapping("/{id}")
    public ResponseEntity<ProcessoResponseDTO> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ProcessoRequestDTO dto) {
        return ResponseEntity.ok(processoService.atualizarProcesso(id, dto));
    }

    // --- Exclusão ---
    @PreAuthorize("hasAnyRole('ADMIN', 'PROTOCOL')")
    @Operation(summary = "Deleta um processo pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        processoService.deletarProcesso(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{processoId}/arquivos/download-zip")
    public ResponseEntity<byte[]> downloadTodosArquivosComoZip(@PathVariable Long processoId) throws IOException {
        // Delegar ao service a montagem do ZIP
        byte[] zipBytes = processoService.gerarZipArquivos(processoId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "processo_" + processoId + "_anexos.zip");
        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
    }

    // Alias para compatibilidade: redireciona /download-rar -> /download-zip
    @GetMapping("/{processoId}/arquivos/download-rar")
    public ResponseEntity<Void> downloadTodosArquivosComoRarAlias(@PathVariable Long processoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, "/api/processos/" + processoId + "/arquivos/download-zip");
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // --- Leitura com filtros e escopo do usuário logado ---
    @Operation(summary = "Lista processos (com filtro principal e filtros adicionais)")
    @GetMapping
    public ResponseEntity<Page<ProcessoResponseDTO>> listarTodosProcessos(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String departamentoOrigem,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false, defaultValue = "todos") String filtro,
            @PageableDefault(sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Usuario)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        Page<ProcessoResponseDTO> processos = processoService.buscarComFiltros(
                filtro, usuarioLogado, status, departamentoOrigem, titulo, pageable
        );

        return ResponseEntity.ok(processos);
    }

    // NOVO ENDPOINT para o SECADM atribuir protocolo e encaminhar
    @PatchMapping("/{id}/protocolar")
    @PreAuthorize("hasRole('SECADM')") // Garante a segurança no nível do endpoint
    public ResponseEntity<ProcessoResponseDTO> protocolarEEncaminhar(
            @PathVariable Long id,
            @Valid @RequestBody ProtocoloUpdateRequestDTO protocoloDTO,
            Authentication authentication) {

        Usuario secadmLogado = (Usuario) authentication.getPrincipal();
        ProcessoResponseDTO processoAtualizado = processoService.atribuirProtocoloEEncaminhar(id, protocoloDTO, secadmLogado);
        return ResponseEntity.ok(processoAtualizado);
    }

    // REMOVIDO: método GET antigo duplicado.
}