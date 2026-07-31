package com.finisus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class OpenApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void publicaEspecificacaoComMetadadosEDefinicaoJwt() throws Exception {
		mockMvc.perform(get("/api/v1/docs")).andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Finisus API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
				.andExpect(jsonPath("$.paths['/api/v1/contas'].get.security[0].bearerAuth").exists());
	}

	@Test
	void disponibilizaSwaggerUiSemToken() throws Exception {
		mockMvc.perform(get("/api/v1/swagger-ui/index.html")).andExpect(status().isOk());
	}
}
