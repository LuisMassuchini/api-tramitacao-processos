package com.camara.processos_api.repository.specs;

import com.camara.processos_api.model.Processo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ProcessoSpecification {
    public static Specification<Processo> comStatus(String status) {
        return (root, query, builder) -> {
            if (status == null || status.isBlank()) {
                return builder.conjunction();
            }
            return builder.equal(root.get("status"), status);
        };
    }

    public static Specification<Processo> comDepartamento(String departamento) {
        return (root, query, builder) -> {
            if (departamento == null || departamento.isBlank()) {
                return builder.conjunction();
            }
            return builder.equal(root.get("departamentoOrigem"), departamento);
        };
    }

    public static Specification<Processo> comTituloContendo(String titulo) {
        return (root, query, builder) -> {
            if (titulo == null || titulo.isBlank()) {
                return builder.conjunction();
            }
            return builder.like(builder.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%");
        };
    }
}