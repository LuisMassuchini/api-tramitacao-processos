package com.camara.processos_api.mapper; // Verifique se o seu pacote está correto

import com.camara.processos_api.dto.ArquivoResponseDTO;
import com.camara.processos_api.model.Arquivo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // Importe a anotação Mapping

@Mapper(componentModel = "spring") // ADICIONE ESTA LINHA
public interface ArquivoMapper {

    @Mapping(source = "enviadoPor.nome", target = "enviadoPorNome")
    @Mapping(source = "processo.id", target = "idProcesso")
    @Mapping(source = "etapa.id", target = "idEtapa")
    ArquivoResponseDTO toResponseDTO(Arquivo arquivo);
}