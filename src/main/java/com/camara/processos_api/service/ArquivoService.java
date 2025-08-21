package com.camara.processos_api.service;

import com.camara.processos_api.dto.ArquivoResponseDTO;
import com.camara.processos_api.dto.ProcessoRequestDTO;
import com.camara.processos_api.dto.ProcessoResponseDTO;
import com.camara.processos_api.exception.FileNotFoundException;
import com.camara.processos_api.exception.ResourceNotFoundException;
import com.camara.processos_api.mapper.ArquivoMapper;
import com.camara.processos_api.mapper.ProcessoMapper;
import com.camara.processos_api.model.Arquivo;
import com.camara.processos_api.model.Etapa;
import com.camara.processos_api.model.Processo;
import com.camara.processos_api.model.Usuario;
import com.camara.processos_api.repository.ArquivoRepository;
import com.camara.processos_api.repository.ProcessoRepository;
import com.camara.processos_api.repository.UsuarioRepository;
import com.camara.processos_api.repository.EtapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArquivoService {

    private final FileStorageService fileStorageService;
    private final ArquivoRepository arquivoRepository;
    private final ProcessoRepository processoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EtapaRepository etapaRepository;
    private final ArquivoMapper arquivoMapper;
    private final ProcessoMapper processoMapper;

    private final ProcessoService processoService;

    @Transactional
    public ArquivoResponseDTO salvarArquivoParaProcesso(MultipartFile file, Long processoId, Long usuarioId) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));
        return salvar(file, processo, null, usuarioId);
    }

    @Transactional
    public ArquivoResponseDTO salvarArquivoParaEtapa(MultipartFile file, Long etapaId, Long usuarioId) {
        Etapa etapa = etapaRepository.findById(etapaId)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa não encontrada"));
        return salvar(file, etapa.getProcesso(), etapa, usuarioId);
    }

    private ArquivoResponseDTO salvar(MultipartFile file, Processo processo, Etapa etapa, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        String caminhoArquivo = fileStorageService.storeFile(file, processo.getId());

        Arquivo arquivo = new Arquivo();
        arquivo.setProcesso(processo);
        arquivo.setEtapa(etapa);
        arquivo.setEnviadoPor(usuario);
        arquivo.setNomeArquivo(file.getOriginalFilename());
        arquivo.setCaminhoArquivo(caminhoArquivo);
        arquivo.setTipo(file.getContentType());
        arquivo.setDataEnvio(LocalDateTime.now());

        Arquivo arquivoSalvo = arquivoRepository.save(arquivo);
        return arquivoMapper.toResponseDTO(arquivoSalvo);
    }

    @Transactional
    public void deletarArquivo(Long arquivoId) {
        Arquivo arquivo = arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new FileNotFoundException("Arquivo não encontrado com id: " + arquivoId));
        fileStorageService.deleteFile(arquivo.getCaminhoArquivo());
        arquivoRepository.delete(arquivo);
    }

    @Transactional
    public Arquivo buscarPorId(Long arquivoId) {
        return arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new FileNotFoundException("Arquivo não encontrado com id: " + arquivoId));
    }

    @Transactional
    public ProcessoResponseDTO criarProcessoComArquivo(ProcessoRequestDTO dto, MultipartFile arquivo, Usuario criadoPor) {
        Processo novoProcesso = processoMapper.toEntity(dto);
        novoProcesso.setCriadoPor(criadoPor);
        novoProcesso.setDataCriacao(LocalDateTime.now());
        novoProcesso.setStatus("Em andamento");
        Processo processoSalvo = processoRepository.save(novoProcesso);

        if (arquivo != null && !arquivo.isEmpty()) {
            String caminhoDoArquivo = fileStorageService.storeFile(arquivo, processoSalvo.getId());
            Arquivo novoArquivo = new Arquivo();
            novoArquivo.setProcesso(processoSalvo);
            novoArquivo.setNomeArquivo(arquivo.getOriginalFilename());
            novoArquivo.setCaminhoArquivo(caminhoDoArquivo);
            novoArquivo.setTipo(arquivo.getContentType());
            novoArquivo.setEnviadoPor(criadoPor);
            novoArquivo.setDataEnvio(LocalDateTime.now());
            arquivoRepository.save(novoArquivo);
        }
        return processoMapper.toResponseDTO(processoSalvo);
    }

    @Transactional
    public List<ArquivoResponseDTO> salvarArquivosParaEtapa(Long etapaId, List<MultipartFile> files, Usuario usuarioLogado) {
        Etapa etapa = etapaRepository.findById(etapaId)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa não encontrada com o ID: " + etapaId));

        // Verifica se o usuário logado é o responsável pelo processo
        processoService.verificarResponsavelPeloProcesso(etapa.getProcesso().getId(), usuarioLogado);

        List<Arquivo> arquivosSalvos = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String caminho = fileStorageService.storeFile(file, etapa.getProcesso().getId());
                Arquivo novoArquivo = new Arquivo();
                novoArquivo.setProcesso(etapa.getProcesso());
                novoArquivo.setEtapa(etapa); // Associa o arquivo à etapa
                novoArquivo.setNomeArquivo(file.getOriginalFilename());
                novoArquivo.setCaminhoArquivo(caminho);
                novoArquivo.setTipo(file.getContentType());
                novoArquivo.setEnviadoPor(usuarioLogado);
                novoArquivo.setDataEnvio(LocalDateTime.now());
                arquivosSalvos.add(arquivoRepository.save(novoArquivo));
            }
        }

        // Converte a lista de entidades para uma lista de DTOs
        return arquivosSalvos.stream()
                .map(arquivoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

}