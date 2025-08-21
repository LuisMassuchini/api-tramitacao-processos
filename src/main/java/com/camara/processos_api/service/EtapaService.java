package com.camara.processos_api.service;

import com.camara.processos_api.dto.EtapaRequestDTO;
import com.camara.processos_api.dto.EtapaResponseDTO;
import com.camara.processos_api.exception.AuthorizationException; // ADICIONADO
import com.camara.processos_api.exception.ResourceNotFoundException;
import com.camara.processos_api.mapper.EtapaMapper;
import com.camara.processos_api.model.Arquivo;
import com.camara.processos_api.model.Etapa;
import com.camara.processos_api.model.Processo;
import com.camara.processos_api.model.Usuario;
import com.camara.processos_api.repository.ArquivoRepository;
import com.camara.processos_api.repository.EtapaRepository;
import com.camara.processos_api.repository.ProcessoRepository;
import com.camara.processos_api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtapaService {

    private final EtapaRepository etapaRepository;
    private final ProcessoRepository processoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EtapaMapper etapaMapper;
    private final NotificacaoService notificacaoService;
    private final PdfGenerationService pdfService;
    private final FileStorageService fileStorageService;
    private final ArquivoRepository arquivoRepository;

    @Transactional
    public EtapaResponseDTO criarEtapa(Long processoId, EtapaRequestDTO dto, Usuario remetente) {
        // Verifica responsável atual: se houver etapa anterior, apenas o destinatário atual pode encaminhar
        var optUltima = etapaRepository.findTopByProcessoIdOrderByDataEnvioDesc(processoId);
        if (optUltima.isPresent()) {
            Etapa ultimaEtapa = optUltima.get();
            if (!ultimaEtapa.getParaUsuario().getId().equals(remetente.getId())) {
                throw new AuthorizationException("Acesso negado. Você não é o responsável atual por este processo.");
            }
        }

        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado com o ID: " + processoId));

        // NOVO: Atualiza e persiste o protocolo se SECADM enviar
        if ("secadm".equalsIgnoreCase(remetente.getPerfil()) && dto.getProtocolo() != null && !dto.getProtocolo().isBlank()) {
            processo.setProtocolo(dto.getProtocolo().trim());
            processoRepository.save(processo);
        }

        Usuario paraUsuario = usuarioRepository.findById(dto.getParaUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de destino não encontrado com o ID: " + dto.getParaUsuarioId()));

        Etapa novaEtapa = new Etapa();
        novaEtapa.setProcesso(processo);

        // Remetente como usuário de origem
        novaEtapa.setDeUsuario(remetente);
        novaEtapa.setDeDepartamento(remetente.getDepartamento());

        // Destinatário conforme DTO
        novaEtapa.setParaUsuario(paraUsuario);
        novaEtapa.setParaDepartamento(dto.getParaDepartamento());

        novaEtapa.setStatus(dto.getStatus());
        novaEtapa.setObservacao(dto.getObservacao());
        // NÃO persistir Base64 no banco para evitar truncation
        novaEtapa.setAssinatura(null);
        novaEtapa.setDataEnvio(LocalDateTime.now());

        processo.setStatus(dto.getStatus());
        processoRepository.save(processo);

        Etapa etapaSalva = etapaRepository.save(novaEtapa);

        String demanda = String.format(
                "O processo #%d ('%s') foi encaminhado para você por %s.",
                processo.getId(), processo.getTitulo(), remetente.getNome()
        );
        String link = "/processos/" + processo.getId();
        notificacaoService.criarNotificacao(remetente, paraUsuario, demanda, link);

        // Seleciona assinatura para o PDF (preferindo a imagem Base64)
        String assinaturaParaPdf = (dto.getAssinaturaImagemBase64() != null && !dto.getAssinaturaImagemBase64().isBlank())
                ? dto.getAssinaturaImagemBase64() : dto.getAssinatura();

        try {
            // Gera o arquivo PDF com os dados da etapa (uma página), usando a assinatura recebida
            File pdfFile = pdfService.gerarPdfDeEtapa(etapaSalva, assinaturaParaPdf, dto.isUsarAssinaturaTexto());

            // Salva o PDF gerado no disco usando nosso serviço de arquivos
            String caminhoPdf;
            try (FileInputStream fis = new FileInputStream(pdfFile)) {
                caminhoPdf = fileStorageService.storeFile(fis, pdfFile.getName(), processoId);
            }

            // Cria um registro de 'Arquivo' para o PDF gerado
            Arquivo arquivoPdf = new Arquivo();
            arquivoPdf.setProcesso(etapaSalva.getProcesso());
            arquivoPdf.setEtapa(etapaSalva); // Associa o PDF à etapa que o gerou
            arquivoPdf.setNomeArquivo(pdfFile.getName());
            arquivoPdf.setCaminhoArquivo(caminhoPdf);
            arquivoPdf.setTipo("application/pdf");
            arquivoPdf.setEnviadoPor(remetente);
            arquivoPdf.setDataEnvio(LocalDateTime.now());
            arquivoRepository.save(arquivoPdf);

            // Remove o arquivo temporário gerado
            pdfFile.delete();

        } catch (IOException e) {
            // Lidar com o erro de geração/armazenamento de PDF
            System.err.println("Erro ao gerar/armazenar o PDF da etapa: " + e.getMessage());
        }

        // Atualiza/gera o PDF consolidado (sempre adiciona nova página)
        atualizarPdfDespachoConsolidado(processoId, etapaSalva, remetente, assinaturaParaPdf, dto.isUsarAssinaturaTexto());

        return etapaMapper.toResponseDTO(etapaSalva);
    }

    @Transactional
    public EtapaResponseDTO criarEtapaComArquivos(Long processoId, EtapaRequestDTO dto, List<MultipartFile> arquivos, Usuario remetente) {
        // Verificação de responsabilidade igual ao método criarEtapa
        var optUltima = etapaRepository.findTopByProcessoIdOrderByDataEnvioDesc(processoId);
        if (optUltima.isPresent()) {
            Etapa ultimaEtapa = optUltima.get();
            if (!ultimaEtapa.getParaUsuario().getId().equals(remetente.getId())) {
                throw new AuthorizationException("Acesso negado. Você não é o responsável atual por este processo.");
            }
        }

        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado com o ID: " + processoId));

        // NOVO: Lógica para atualizar o protocolo se o usuário for o 'secadm'
        if ("secadm".equalsIgnoreCase(remetente.getPerfil()) && dto.getProtocolo() != null && !dto.getProtocolo().isBlank()) {
            processo.setProtocolo(dto.getProtocolo().trim());
            processoRepository.save(processo); // Salva a atualização do protocolo no processo principal
        }

        Usuario paraUsuario = usuarioRepository.findById(dto.getParaUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de destino não encontrado com o ID: " + dto.getParaUsuarioId()));

        Etapa novaEtapa = new Etapa();
        novaEtapa.setProcesso(processo);
        novaEtapa.setDeUsuario(remetente);
        novaEtapa.setDeDepartamento(remetente.getDepartamento());
        novaEtapa.setParaUsuario(paraUsuario);
        novaEtapa.setParaDepartamento(dto.getParaDepartamento());
        novaEtapa.setStatus(dto.getStatus());
        novaEtapa.setObservacao(dto.getObservacao());
        // NÃO persistir Base64 no banco
        novaEtapa.setAssinatura(null);
        novaEtapa.setDataEnvio(LocalDateTime.now());

        processo.setStatus(dto.getStatus());
        processoRepository.save(processo);

        Etapa etapaSalva = etapaRepository.save(novaEtapa);

        // Notificação
        String demanda = String.format(
                "O processo #%d ('%s') foi encaminhado para você por %s.",
                processo.getId(), processo.getTitulo(), remetente.getNome()
        );
        String link = "/processos/" + processo.getId();
        notificacaoService.criarNotificacao(remetente, paraUsuario, demanda, link);

        String assinaturaParaPdf = (dto.getAssinaturaImagemBase64() != null && !dto.getAssinaturaImagemBase64().isBlank())
                ? dto.getAssinaturaImagemBase64() : dto.getAssinatura();

        // Atualiza/gera o PDF consolidado (sempre adiciona nova página)
        atualizarPdfDespachoConsolidado(processoId, etapaSalva, remetente, assinaturaParaPdf, dto.isUsarAssinaturaTexto());

        // Salvar anexos enviados junto com a etapa (se houver)
        if (arquivos != null && !arquivos.isEmpty()) {
            for (MultipartFile file : arquivos) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String caminho = fileStorageService.storeFile(file, processoId);
                        Arquivo novoArquivo = new Arquivo();
                        novoArquivo.setProcesso(etapaSalva.getProcesso());
                        novoArquivo.setEtapa(etapaSalva);
                        novoArquivo.setNomeArquivo(file.getOriginalFilename());
                        novoArquivo.setCaminhoArquivo(caminho);
                        novoArquivo.setTipo(file.getContentType());
                        novoArquivo.setEnviadoPor(remetente);
                        novoArquivo.setDataEnvio(LocalDateTime.now());
                        arquivoRepository.save(novoArquivo);
                    } catch (Exception ex) {
                        System.err.println("[WARN] Falha ao salvar anexo da etapa: " + ex.getMessage());
                    }
                }
            }
        }

        return etapaMapper.toResponseDTO(etapaSalva);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<EtapaResponseDTO> listarEtapasPorProcesso(Long processoId) {
        if (!processoRepository.existsById(processoId)) {
            throw new ResourceNotFoundException("Processo não encontrado com o ID: " + processoId);
        }
        List<Etapa> etapas = etapaRepository.findByProcessoIdOrderByIdDesc(processoId);
        return etapas.stream().map(etapaMapper::toResponseDTO).collect(Collectors.toList());
    }

    // Método utilitário centralizado para manter uma única fonte de verdade
    private void atualizarPdfDespachoConsolidado(Long processoId, Etapa etapaSalva, Usuario remetente, String assinaturaBase64, boolean usarAssinaturaTexto) {
        try {
            // 1. Procura pelo PDF consolidado existente
            Optional<Arquivo> ultimoPdfOpt = arquivoRepository.findTopByProcessoIdAndTipoAndNomeArquivoStartingWithOrderByIdDesc(
                    processoId, "application/pdf", "despacho_processo_"
            );

            File pdfExistente = ultimoPdfOpt.map(a -> new File(a.getCaminhoArquivo())).orElse(null);

            // 2. Gera um novo PDF com todas as páginas anteriores + a página nova
            File pdfAtualizadoFile = pdfService.adicionarPaginaDeDespacho(pdfExistente, etapaSalva, assinaturaBase64, usarAssinaturaTexto);

            // 3. Salva fisicamente no diretório do processo
            String caminhoNovoPdf;
            try (FileInputStream fis = new FileInputStream(pdfAtualizadoFile)) {
                caminhoNovoPdf = fileStorageService.storeFile(fis, pdfAtualizadoFile.getName(), processoId);
            }

            // 4. Atualiza ou cria o registro no banco
            Arquivo registroPdf = ultimoPdfOpt.orElseGet(Arquivo::new);
            registroPdf.setProcesso(etapaSalva.getProcesso());
            registroPdf.setEtapa(etapaSalva);
            registroPdf.setNomeArquivo(pdfAtualizadoFile.getName());
            registroPdf.setCaminhoArquivo(caminhoNovoPdf);
            registroPdf.setTipo("application/pdf");
            registroPdf.setEnviadoPor(remetente);
            registroPdf.setDataEnvio(LocalDateTime.now());
            arquivoRepository.save(registroPdf);

            // 5. Limpa o arquivo temporário
            if (!pdfAtualizadoFile.delete()) {
                System.err.println("[WARN] Não foi possível excluir o arquivo temporário do PDF: " + pdfAtualizadoFile.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("ERRO CRÍTICO ao atualizar o PDF de despacho: " + e.getMessage());
        }
    }
}
