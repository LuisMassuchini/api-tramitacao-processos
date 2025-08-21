package com.camara.processos_api.repository;

import com.camara.processos_api.model.Processo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long>, JpaSpecificationExecutor<Processo> {
    @Query("SELECT p FROM Processo p " +
            "LEFT JOIN FETCH p.criadoPor " +
            "LEFT JOIN FETCH p.etapas e " +
            "WHERE p.id = :id")
    Optional<Processo> findByIdWithDetails(@Param("id") Long id);

    //NOVO: Query para "Caixa de Entrada" (Para Mim)
    // Encontra processos onde o usuário :usuarioId é o destinatário da etapa mais recente.
    @Query(
            value = "SELECT p.* FROM processos_tramitacao p " +
                    "INNER JOIN etapas_processo e ON p.id = e.id_processo " +
                    "WHERE e.para_usuario = :usuarioId AND e.id = (" +
                    "  SELECT MAX(e2.id) FROM etapas_processo e2 WHERE e2.id_processo = p.id" +
                    ") ORDER BY p.data_criacao DESC",
            countQuery = "SELECT count(*) FROM processos_tramitacao p " +
                    "INNER JOIN etapas_processo e ON p.id = e.id_processo " +
                    "WHERE e.para_usuario = :usuarioId AND e.id = (" +
                    "  SELECT MAX(e2.id) FROM etapas_processo e2 WHERE e2.id_processo = p.id" +
                    ")",
            nativeQuery = true // Informa ao Spring que esta é uma query SQL nativa
    )
    Page<Processo> findProcessosParaMim(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // NOVO: Query para "Enviados"
    // Encontra processos distintos (DISTINCT) que tenham PELO MENOS UMA etapa onde o usuário :usuarioId foi o remetente.
    @Query("SELECT DISTINCT p FROM Processo p JOIN p.etapas e WHERE e.deUsuario.id = :usuarioId")
    Page<Processo> findProcessosEnviadosPorMim(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // Detalhes com ETAPAS (sem arquivos)
    @Query("SELECT p FROM Processo p LEFT JOIN FETCH p.etapas WHERE p.id = :id")
    Optional<Processo> findByIdWithEtapas(@Param("id") Long id);

    // Carrega ARQUIVOS para uma lista de processos (merge em cache de persistência)
    @Query("SELECT DISTINCT p FROM Processo p LEFT JOIN FETCH p.arquivos WHERE p IN :processos")
    List<Processo> findWithArquivos(@Param("processos") List<Processo> processos);
}
