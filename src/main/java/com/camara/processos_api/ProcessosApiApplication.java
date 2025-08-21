package com.camara.processos_api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class ProcessosApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessosApiApplication.class, args);
	}

	// ADICIONE ESTE MÉTODO "BEAN" DENTRO DA CLASSE
	@Bean
	public CommandLineRunner commandLineRunner(JdbcTemplate jdbcTemplate) {
		return args -> {
			System.out.println("\n--- VERIFICANDO CONEXÃO COM O BANCO DE DADOS ---");
			try {
				// Pergunta ao banco de dados qual é o nome dele
				String dbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
				System.out.println("[INFO] Conectado com sucesso ao banco de dados: " + dbName);

				// Executa a mesma query que falhou para ver o que o Java encontra
				Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuario WHERE matricula = '785'", Integer.class);
				System.out.println("[INFO] Busca por matrícula '785' encontrou: " + userCount + " usuário(s).");

			} catch (Exception e) {
				System.err.println("[ERRO] Falha ao verificar o banco de dados: " + e.getMessage());
			}
			System.out.println("--------------------------------------------------\n");
		};
	}

}
