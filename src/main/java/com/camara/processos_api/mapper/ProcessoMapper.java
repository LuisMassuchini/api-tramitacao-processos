package com.camara.processos_api.mapper;

import com.camara.processos_api.dto.ProcessoRequestDTO;
import com.camara.processos_api.dto.ProcessoResponseDTO;
import com.camara.processos_api.model.Processo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.camara.processos_api.dto.ArquivoResponseDTO;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {EtapaMapper.class, ArquivoMapper.class})
public interface ProcessoMapper {

    // 1. Mapeamento de aninhamento:
    // Dizemos ao MapStruct que a "fonte" (source) do campo 'nomeCriador' no DTO
    // é a propriedade 'criadoPor.nome' na entidade Processo.
    @Mapping(source = "criadoPor.nome", target = "nomeCriador")
    ProcessoResponseDTO toResponseDTO(Processo processo);

    // 2. Mapeamento para ignorar campos:
    // Dizemos ao MapStruct para ignorar certos campos da entidade 'Processo'
    // ao converter a partir do DTO, pois eles serão definidos no serviço
    // (como 'id', 'status', 'dataCriacao', 'criadoPor') ou não existem no DTO.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "criadoPor", ignore = true)
    @Mapping(target = "departamentoOrigem", ignore = true) // Ignoramos, pois será pego do usuário logado
    @Mapping(target = "etapas", ignore = true)
    @Mapping(target = "arquivos", ignore = true)
    Processo toEntity(ProcessoRequestDTO requestDTO);
}