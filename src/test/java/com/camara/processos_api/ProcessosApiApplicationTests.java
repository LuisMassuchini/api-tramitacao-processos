package com.camara.processos_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.NONE,
	properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.flywaydb.core.api.configuration.ClassicConfiguration"
	}
)
@ActiveProfiles("test")
class ProcessosApiApplicationTests {

	@Test
	void contextLoads() {
		// Teste mínimo: apenas verifica que configuração básica sobe sem DataSource
	}
}
