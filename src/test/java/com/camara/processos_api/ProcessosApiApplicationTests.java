package com.camara.processos_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ProcessosApiApplicationTests {

	@Test
	void contextLoads() {
		// contexto deve subir usando H2
	}

}
