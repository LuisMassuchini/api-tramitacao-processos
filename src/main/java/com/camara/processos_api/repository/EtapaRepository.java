package com.camara.processos_api.repository;

import com.camara.processos_api.model.Etapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtapaRepository extends JpaRepository<Etapa, Long> {
    // Última etapa por maior ID
    Optional<Etapa> findTopByProcessoIdOrderByIdDesc(Long processoId);

    // Última etapa por data de envio mais recente
    Optional<Etapa> findTopByProcessoIdOrderByDataEnvioDesc(Long processoId);

    // Histórico ordenado por data de envio desc
    List<Etapa> findByProcessoIdOrderByDataEnvioDesc(Long processoId);

    // Histórico por ID desc (compatibilidade)
    List<Etapa> findByProcessoIdOrderByIdDesc(Long processoId);
}
