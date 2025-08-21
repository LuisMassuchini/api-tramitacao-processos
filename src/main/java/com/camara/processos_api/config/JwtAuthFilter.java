package com.camara.processos_api.config; // Verifique seu pacote

import com.camara.processos_api.service.JwtService;
import com.camara.processos_api.repository.UsuarioRepository;
import com.camara.processos_api.model.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository; // novo fallback por ID

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("--- NOVO REQUEST: " + request.getRequestURI() + " ---");

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[DEBUG] Cabeçalho 'Authorization' ausente ou não começa com 'Bearer'. Continuando sem autenticação.");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        System.out.println("[DEBUG] Token JWT recebido: " + jwt);

        String userMatricula = null;
        Long uid = null;

        try {
            userMatricula = jwtService.extractUsername(jwt);
            uid = jwtService.extractUserId(jwt);
            System.out.println("[DEBUG] Matrícula extraída do token: " + userMatricula + (uid != null ? ", uid: " + uid : ""));
        } catch (ExpiredJwtException e) {
            System.err.println("[ERRO] O token JWT expirou: " + e.getMessage());
        } catch (SignatureException e) {
            System.err.println("[ERRO] A assinatura do token JWT é inválida: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERRO] Erro ao processar o token JWT: " + e.getMessage());
        }

        if (userMatricula != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("[DEBUG] Buscando usuário no banco com a matrícula: " + userMatricula);

            UserDetails userDetails = null;

            // 1) Tentar por matrícula (subject)
            try {
                userDetails = this.userDetailsService.loadUserByUsername(userMatricula == null ? "" : userMatricula.trim());
                if (userDetails != null) {
                    System.out.println("[DEBUG] Usuário encontrado por matrícula: " + userDetails.getUsername());
                }
            } catch (Exception ignored) { }

            // 2) Fallback por UID (claim uid)
            if (userDetails == null && uid != null) {
                usuarioRepository.findById(uid).ifPresent(u -> {
                    System.out.println("[DEBUG] Fallback por UID funcionou: usuário " + u.getMatricula());
                });
                userDetails = usuarioRepository.findById(uid).orElse(null);
            }

            // 3) Se nenhum encontrado, logar erro
            if (userDetails == null) {
                System.err.println("[ERRO] Usuário com matrícula '" + userMatricula + "' não foi encontrado no banco de dados.");
            }

            if (userDetails != null && jwtService.isTokenValid(jwt, userDetails)) {
                System.out.println("[DEBUG] Token é válido. Autenticando usuário.");
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println("[DEBUG] Token considerado inválido ou usuário não encontrado. Não autenticado.");
            }
        }

        filterChain.doFilter(request, response);
    }
}