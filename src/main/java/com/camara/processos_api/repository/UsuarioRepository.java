package com.camara.processos_api.repository;

import com.camara.processos_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    Optional<Usuario> findByMatricula(String matricula);

    List<Usuario> findByPerfilIn(List<String> perfis);

    // Filtro case-insensitive e ignorando espaços
    @Query("select u from Usuario u where lower(trim(u.perfil)) in :perfis")
    List<Usuario> findByPerfilInIgnoreCase(@Param("perfis") List<String> perfisLower);

    // Para obter rapidamente um usuário por perfil específico
    Optional<Usuario> findFirstByPerfil(String perfil);

    // Busca por matrícula ignorando espaços nas extremidades
    @Query("select u from Usuario u where trim(u.matricula) = :matricula")
    Optional<Usuario> findByMatriculaTrimmed(@Param("matricula") String matricula);
}
