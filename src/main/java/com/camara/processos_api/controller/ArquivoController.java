package com.camara.processos_api.controller;

import com.camara.processos_api.dto.ArquivoResponseDTO;
import com.camara.processos_api.model.Arquivo;
import com.camara.processos_api.service.ArquivoService;
import com.camara.processos_api.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ContentDisposition;
import org.springframework.util.MimeTypeUtils;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ArquivoController {

    private final ArquivoService arquivoService;
    private final FileStorageService fileStorageService; // Para o download

    // Endpoint que substitui o `processos_tramitacao_upload.php`
    @PostMapping("/processos/{processoId}/arquivos")
    public ResponseEntity<ArquivoResponseDTO> uploadArquivo(@PathVariable Long processoId,
                                                 @RequestParam("arquivo") MultipartFile file,
                                                 @RequestParam("usuarioId") Long usuarioId) { // Recebe o ID do usuário que envia
        ArquivoResponseDTO arquivoSalvo = arquivoService.salvarArquivoParaProcesso(file, processoId, usuarioId);
        return ResponseEntity.ok(arquivoSalvo);
    }

    // Endpoint que substitui o `processos_tramitacao_excluir_arquivo.php`
    @DeleteMapping("/arquivos/{arquivoId}")
    public ResponseEntity<Void> deletarArquivo(@PathVariable Long arquivoId) {
        arquivoService.deletarArquivo(arquivoId);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para Download
//    @GetMapping("/arquivos/download/{arquivoId}")
//    public ResponseEntity<Resource> downloadArquivo(@PathVariable Long arquivoId, HttpServletRequest request) throws IOException {
//        Arquivo arquivo = arquivoService.buscarPorId(arquivoId); // Você precisará criar este método no ArquivoService
//        Resource resource = fileStorageService.loadFileAsResource(arquivo.getCaminhoArquivo());
//
//        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//        if(contentType == null) {
//            contentType = "application/octet-stream";
//        }
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    }


    // NOVO Endpoint para anexar arquivo a uma ETAPA
    @PostMapping(value = "/etapas/{etapaId}/arquivos", consumes = "multipart/form-data")
    public ResponseEntity<ArquivoResponseDTO> uploadArquivoParaEtapa(
            @PathVariable Long etapaId,
            @RequestParam("arquivo") MultipartFile file,
            @RequestParam("usuarioId") Long usuarioId) {
        ArquivoResponseDTO arquivoSalvo = arquivoService.salvarArquivoParaEtapa(file, etapaId, usuarioId);
        return ResponseEntity.ok(arquivoSalvo);
    }

    @GetMapping({"/download/{arquivoId}", "/arquivos/download/{arquivoId}"})
    public ResponseEntity<Resource> downloadArquivo(@PathVariable Long arquivoId) {
        System.out.println("\n--- INICIANDO DOWNLOAD/VISUALIZAÇÃO DO ARQUIVO ID: " + arquivoId + " ---");
        try {
            // Passo 1: Busca no banco
            System.out.println("[DEBUG] 1. Buscando informações do arquivo no banco...");
            Arquivo arquivo = arquivoService.buscarPorId(arquivoId);
            if (arquivo == null) {
                System.err.println("[ERRO] ArquivoService.buscarPorId retornou nulo.");
                return ResponseEntity.notFound().build();
            }
            System.out.println("[DEBUG] ... Arquivo encontrado. Caminho: " + arquivo.getCaminhoArquivo());

            // Passo 2: Carrega do disco
            System.out.println("[DEBUG] 2. Carregando o arquivo físico do disco...");
            Resource resource = fileStorageService.loadFileAsResource(arquivo.getCaminhoArquivo());
            if (resource == null || !resource.exists()) {
                System.err.println("[ERRO] FileStorageService não encontrou o recurso no disco.");
                return ResponseEntity.notFound().build();
            }
            System.out.println("[DEBUG] ... Recurso do arquivo carregado. Existe: " + resource.exists());

            // Passo 3: Determina o tipo de mídia
            System.out.println("[DEBUG] 3. Determinando o MediaType. Tipo salvo no banco: " + arquivo.getTipo());
            MediaType mediaType = MediaType.parseMediaType(
                    (arquivo.getTipo() != null && !arquivo.getTipo().isBlank()) ? arquivo.getTipo() : "application/octet-stream"
            );
            System.out.println("[DEBUG] ... MediaType definido como: " + mediaType);

            // Passo 4: Cria os cabeçalhos
            System.out.println("[DEBUG] 4. Criando os cabeçalhos HTTP...");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);

            ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                    .filename(resource.getFilename(), StandardCharsets.UTF_8)
                    .build();
            headers.setContentDisposition(contentDisposition);
            System.out.println("[DEBUG] ... Cabeçalhos criados.");

            // Passo 5: Retorna a resposta
            System.out.println("[DEBUG] 5. Enviando resposta 200 OK para o navegador.");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            System.err.println("[ERRO] Uma exceção inesperada ocorreu durante o download!");
            // Imprime o erro completo no console para análise
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}