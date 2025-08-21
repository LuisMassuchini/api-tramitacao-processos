package com.camara.processos_api.service;

import com.camara.processos_api.dto.EtapaRequestDTO;
import com.camara.processos_api.dto.ProcessoRequestDTO;
import com.camara.processos_api.dto.ProcessoResponseDTO;
import com.camara.processos_api.dto.ProtocoloUpdateRequestDTO;
import com.camara.processos_api.exception.AuthorizationException;
import com.camara.processos_api.exception.ResourceNotFoundException;
import com.camara.processos_api.mapper.ProcessoMapper;
import com.camara.processos_api.model.Processo;
import com.camara.processos_api.model.Usuario;
import com.camara.processos_api.repository.EtapaRepository;
import com.camara.processos_api.repository.ProcessoRepository;
import com.camara.processos_api.repository.UsuarioRepository;
import com.camara.processos_api.repository.specs.ProcessoSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessoService {

    private final ProcessoRepository processoRepository;
    private final FileStorageService fileStorageService;
    private final ProcessoMapper processoMapper;
    private final EtapaService etapaService;
    private final UsuarioRepository usuarioRepository;
    private final EtapaRepository etapaRepository;
    private final ZipService zipService;

    // --- Helpers ---
    @Transactional(Transactional.TxType.SUPPORTS)
    private Page<ProcessoResponseDTO> carregarDetalhesDaPagina(Page<Processo> page) {
        List<Processo> processos = page.getContent();
        if (!processos.isEmpty()) {
            // Carrega ARQUIVOS para todos os processos da página em uma única query
            processoRepository.findWithArquivos(processos);
        }
        return page.map(processoMapper::toResponseDTO);
    }

    // --- MÉTODOS DE LEITURA ---
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<ProcessoResponseDTO> buscarComFiltros(
            String filtro, Usuario usuarioLogado, String status,
            String depto, String titulo, Pageable pageable) {

        Page<Processo> processosPage;

        switch (filtro.toLowerCase()) {
            case "para_mim":
                // Evita empurrar Sort do Pageable para a native query (que causava p.dataCriacao)
                Pageable semOrdenacao = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
                processosPage = processoRepository.findProcessosParaMim(usuarioLogado.getId(), semOrdenacao);
                break;

            case "meus_enviados":
                processosPage = processoRepository.findProcessosEnviadosPorMim(usuarioLogado.getId(), pageable);
                break;

            case "todos":
            default:
                Specification<Processo> spec = Specification.where(ProcessoSpecification.comStatus(status))
                        .and(ProcessoSpecification.comDepartamento(depto))
                        .and(ProcessoSpecification.comTituloContendo(titulo));
                processosPage = processoRepository.findAll(spec, pageable);
                break;
        }

        // Carrega arquivos para os processos desta página (duas consultas)
        return carregarDetalhesDaPagina(processosPage);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public ProcessoResponseDTO buscarPorId(Long id) {
        // 1) Carrega processo + ETAPAS
        Processo processo = processoRepository.findByIdWithEtapas(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado com o ID: " + id));
        // 2) Carrega ARQUIVOS para este processo
        processoRepository.findWithArquivos(java.util.List.of(processo));
        return processoMapper.toResponseDTO(processo);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public byte[] gerarZipArquivos(Long processoId) throws IOException {
        Processo processo = processoRepository.findByIdWithEtapas(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));
        processoRepository.findWithArquivos(java.util.List.of(processo));
        return zipService.criarZipDeArquivos(new java.util.LinkedHashSet<>(processo.getArquivos()));
    }

    // --- CRIAÇÃO SEM ARQUIVO ---
    @Transactional
    public ProcessoResponseDTO criarProcesso(ProcessoRequestDTO dto, Usuario criadoPor) {
        Processo processo = processoMapper.toEntity(dto);

        processo.setCriadoPor(criadoPor);
        processo.setDepartamentoOrigem(criadoPor.getDepartamento());
        processo.setDataCriacao(LocalDateTime.now());
        Processo salvo = processoRepository.save(processo);

        // 1ª etapa via service (gera PDF)
        Usuario secadm = usuarioRepository.findFirstByPerfil("secadm")
                .orElseThrow(() -> new IllegalStateException("Usuário 'secadm' não encontrado. Não é possível protocolar o processo."));
        EtapaRequestDTO primeira = new EtapaRequestDTO();
        primeira.setParaUsuarioId(secadm.getId());
        primeira.setParaDepartamento(secadm.getDepartamento());
        primeira.setStatus("Protocolado - Aguardando Distribuição");
        primeira.setObservacao("Criação e envio inicial do processo para o Secretário Administrativo.");
        // Propaga assinatura do processo para a etapa
        primeira.setAssinaturaImagemBase64(dto.getAssinaturaImagemBase64());
        primeira.setAssinatura(null);
        etapaService.criarEtapa(salvo.getId(), primeira, criadoPor);

        // Retorna com duas consultas para evitar problemas de fetch
        Processo completo = processoRepository.findByIdWithEtapas(salvo.getId()).orElse(salvo);
        processoRepository.findWithArquivos(java.util.List.of(completo));
        return processoMapper.toResponseDTO(completo);
    }

    // --- CRIAÇÃO COM MÚLTIPLOS ARQUIVOS ---
    @Transactional
    public ProcessoResponseDTO criarProcessoComArquivo(ProcessoRequestDTO dto, List<MultipartFile> arquivos, Usuario criadoPor) {
        Processo novo = processoMapper.toEntity(dto);
        novo.setCriadoPor(criadoPor);
        novo.setDepartamentoOrigem(criadoPor.getDepartamento());
        novo.setDataCriacao(LocalDateTime.now());
        Processo salvo = processoRepository.save(novo);

        Usuario secadm = usuarioRepository.findFirstByPerfil("secadm")
                .orElseThrow(() -> new IllegalStateException("Usuário 'secadm' não encontrado. Não é possível protocolar o processo."));
        EtapaRequestDTO primeira = new EtapaRequestDTO();
        primeira.setParaUsuarioId(secadm.getId());
        primeira.setParaDepartamento(secadm.getDepartamento());
        primeira.setStatus("Protocolado - Aguardando Distribuição");
        primeira.setObservacao("Criação e envio inicial do processo para o Secretário Administrativo.");
        // Propaga assinatura do processo para a etapa
        primeira.setAssinaturaImagemBase64(dto.getAssinaturaImagemBase64());
        primeira.setAssinatura(null);
        etapaService.criarEtapaComArquivos(salvo.getId(), primeira, arquivos, criadoPor);

        Processo completo = processoRepository.findByIdWithEtapas(salvo.getId()).orElse(salvo);
        processoRepository.findWithArquivos(java.util.List.of(completo));
        return processoMapper.toResponseDTO(completo);
    }

    // --- ATUALIZAÇÃO ---
    @Transactional
    public ProcessoResponseDTO atualizarProcesso(Long id, ProcessoRequestDTO dto) {
        Processo existente = processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado com o ID: " + id));

        existente.setTitulo(dto.getTitulo());
        existente.setDescricao(dto.getDescricao());

        Processo salvo = processoRepository.save(existente);
        Processo completo = processoRepository.findByIdWithEtapas(salvo.getId()).orElse(salvo);
        processoRepository.findWithArquivos(java.util.List.of(completo));
        return processoMapper.toResponseDTO(completo);
    }

    // --- EXCLUSÃO ---
    @Transactional
    public void deletarProcesso(Long id) {
        if (!processoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Processo não encontrado com o ID: " + id);
        }
        processoRepository.deleteById(id);
    }

    public void verificarResponsavelPeloProcesso(Long processoId, Usuario usuario) {
        var ultima = etapaRepository.findTopByProcessoIdOrderByDataEnvioDesc(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("O processo não possui etapas para determinar um responsável."));
        if (!ultima.getParaUsuario().getId().equals(usuario.getId())) {
            throw new AuthorizationException("Acesso negado. Você não é o responsável atual por este processo.");
        }
    }

    @Transactional
    public ProcessoResponseDTO atribuirProtocoloEEncaminhar(Long processoId, ProtocoloUpdateRequestDTO dto, Usuario secadm) {
        if (!"secadm".equalsIgnoreCase(secadm.getPerfil())) {
            throw new AuthorizationException("Apenas o Secretário Administrativo pode atribuir um protocolo.");
        }

        verificarResponsavelPeloProcesso(processoId, secadm);

        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado com o ID: " + processoId));
        processo.setProtocolo(dto.getProtocolo());
        processoRepository.save(processo);

        EtapaRequestDTO novaEtapaDto = new EtapaRequestDTO();
        novaEtapaDto.setParaUsuarioId(dto.getProximoDestinatarioId());

        Usuario proximoDestinatario = usuarioRepository.findById(dto.getProximoDestinatarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Próximo destinatário não encontrado."));
        novaEtapaDto.setParaDepartamento(proximoDestinatario.getDepartamento());

        novaEtapaDto.setStatus(dto.getStatusNovaEtapa());
        novaEtapaDto.setObservacao(dto.getObservacaoNovaEtapa());

        etapaService.criarEtapa(processoId, novaEtapaDto, secadm);

        Processo completo = processoRepository.findByIdWithEtapas(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado após atualização."));
        processoRepository.findWithArquivos(java.util.List.of(completo));
        return processoMapper.toResponseDTO(completo);
    }
}
