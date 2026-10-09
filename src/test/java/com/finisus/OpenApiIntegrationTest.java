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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class OpenApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void publicaEspecificacaoComMetadadosEDefinicaoJwt() throws Exception {
		mockMvc.perform(get("/api/v1/docs").with(
				jwt().authorities(new SimpleGrantedAuthority("DOCUMENTACAO_API_LER"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Finisus API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
				.andExpect(jsonPath("$.paths['/api/v1/contas'].get.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/contas/{contaId}/reconciliacao'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/contas/{contaId}/ajustes-saldo'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/dashboard/visao-geral'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/dashboard/receitas-gastos'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/dashboard/compartilhados'].get").exists())
				.andExpect(jsonPath("$.components.schemas.PatrimonioResponse.properties.saldosEDividasConsultadosEm").exists())
				.andExpect(jsonPath("$.components.schemas.PatrimonioResponse.properties.patrimonioHistoricoCompleto").exists())
				.andExpect(jsonPath("$.paths['/api/v1/obrigacoes-financeiras'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/transferencias'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/transferencias/{transferenciaId}/estornar'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/obrigacoes-financeiras/{obrigacaoId}/pagamentos'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/obrigacoes-financeiras/{obrigacaoId}/pagamentos/{pagamentoId}/estornar'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/cartoes/faturas/processar-ciclos'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/divisoes-compartilhadas/{divisaoId}/resumo'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/usuarios/me/dados'].get.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/usuarios/me/solicitacoes-anonimizacao'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/usuarios/me/solicitacoes-privacidade'].get").exists());
	}

	@Test
	void disponibilizaSwaggerUiSomenteComPermissao() throws Exception {
		mockMvc.perform(get("/api/v1/swagger-ui/index.html")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/v1/swagger-ui/index.html").with(
				jwt().authorities(new SimpleGrantedAuthority("DOCUMENTACAO_API_LER"))))
				.andExpect(status().isOk());
	}

	@Test
	void publicaCodigoEstavelQuandoAutenticacaoEstaAusente() throws Exception {
		mockMvc.perform(get("/api/v1/contas")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.type").value("urn:finisus:problem:error.auth.unauthorized"))
				.andExpect(jsonPath("$.code").value("error.auth.unauthorized"));
	}
}
