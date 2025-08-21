package com.camara.processos_api.repository;

import com.camara.processos_api.model.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArquivoRepository  extends JpaRepository<Arquivo, Long> {

    // NOVO MÉTODO: Busca o arquivo mais recente de um processo que seja do tipo "application/pdf"
    // e cujo nome comece com "despacho_etapa_".
    Optional<Arquivo> findTopByProcessoIdAndTipoAndNomeArquivoStartingWithOrderByIdDesc(
            Long processoId, String tipo, String prefixoNome
    );
}
