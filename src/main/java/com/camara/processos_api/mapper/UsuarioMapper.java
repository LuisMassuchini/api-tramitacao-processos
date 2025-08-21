package com.camara.processos_api.mapper;

import com.camara.processos_api.dto.UsuarioResponseDTO;
import com.camara.processos_api.model.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioResponseDTO toResponseDTO(Usuario usuario);
}