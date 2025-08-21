package com.camara.processos_api.model;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matricula;
    private String nome;
    private String perfil;
    private String email;
    private String departamento;
    private String senha;

    // Métodos da interface UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // O Spring Security espera que as permissões comecem com o prefixo "ROLE_"
        // Esta linha transforma o perfil "admin" em uma permissão "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.perfil.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        // Usaremos a matrícula como o "nome de usuário" principal
        return matricula;
    }

    // Para simplificar, vamos retornar 'true' para todos os campos de status
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    // Getters explícitos para MapStruct
    public Long getId() { return id; }
    public String getMatricula() { return matricula; }
    public String getNome() { return nome; }
    public String getPerfil() { return perfil; }
    public String getEmail() { return email; }
    public String getDepartamento() { return departamento; }
    public String getSenha() { return senha; }
}
