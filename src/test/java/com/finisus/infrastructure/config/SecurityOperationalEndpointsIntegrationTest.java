package com.finisus.infrastructure.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finisus.TestJwtKeyConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class SecurityOperationalEndpointsIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthPermanecePublico() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
	}

	@Test
	void metricasExigemPermissaoEspecifica() throws Exception {
		mockMvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/actuator/metrics").with(jwt())).andExpect(status().isForbidden());
		mockMvc.perform(get("/actuator/metrics").with(
				jwt().authorities(new SimpleGrantedAuthority("OBSERVABILIDADE_LER"))))
				.andExpect(status().isOk());
	}

	@Test
	void documentacaoExigePermissaoEspecifica() throws Exception {
		mockMvc.perform(get("/api/v1/docs")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/v1/docs").with(jwt())).andExpect(status().isForbidden());
		mockMvc.perform(get("/api/v1/docs").with(
				jwt().authorities(new SimpleGrantedAuthority("DOCUMENTACAO_API_LER"))))
				.andExpect(status().isOk());
	}
}
