package com.camara.processos_api.mapper;

import com.camara.processos_api.dto.EtapaResponseDTO;
import com.camara.processos_api.model.Etapa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EtapaMapper {

    // Mapeamento manual para evitar erro de propriedade aninhada
    default EtapaResponseDTO toResponseDTO(Etapa etapa) {
        if (etapa == null) return null;
        EtapaResponseDTO dto = new EtapaResponseDTO();
        dto.setId(etapa.getId());
        dto.setDeUsuarioNome(etapa.getDeUsuario() != null ? etapa.getDeUsuario().getNome() : null);
        dto.setParaUsuarioNome(etapa.getParaUsuario() != null ? etapa.getParaUsuario().getNome() : null);
        // novo: id do usuário destinatário
        dto.setParaUsuarioId(etapa.getParaUsuario() != null ? etapa.getParaUsuario().getId() : null);
        dto.setDeDepartamento(etapa.getDeDepartamento());
        dto.setParaDepartamento(etapa.getParaDepartamento());
        dto.setStatus(etapa.getStatus());
        dto.setObservacao(etapa.getObservacao());
        dto.setAssinatura(etapa.getAssinatura());
        dto.setDataEnvio(etapa.getDataEnvio());

        // Adicione outros campos conforme necessário
        return dto;
    }
}
