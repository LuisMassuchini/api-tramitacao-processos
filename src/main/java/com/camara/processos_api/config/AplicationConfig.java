package com.camara.processos_api.config;

import com.camara.processos_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
// @Configuration // removido para evitar duplicação de beans após correção de nome
@RequiredArgsConstructor
class AplicationConfigLegacy { // Classe legacy sem uso; pacote-privada para evitar erro de compilação

    private final UsuarioRepository usuarioRepository; // mantido apenas para referência; não será usado
}