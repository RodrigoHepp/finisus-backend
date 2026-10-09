package com.finisus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finisus.adapters.in.web.AuthController;
import com.finisus.adapters.in.web.security.UsuarioAtual;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class OwnershipWebContractIntegrationTest {
	@Autowired
	@Qualifier("requestMappingHandlerMapping")
	private RequestMappingHandlerMapping mappings;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void todaRotaFinanceiraPropagaOUsuarioAutenticadoAoCasoDeUso() {
		var semUsuario = new TreeSet<String>();
		mappings.getHandlerMethods().forEach((mapping, handler) -> {
			Class<?> controller = handler.getBeanType();
			if (!controller.getPackageName().equals("com.finisus.adapters.in.web")) return;
			if (controller == AuthController.class && !handler.getMethod().getName().equals("cadastrar")) return;
			boolean possuiUsuario = Arrays.stream(handler.getMethod().getParameters())
					.anyMatch(parametro -> parametro.isAnnotationPresent(UsuarioAtual.class));
			if (!possuiUsuario) semUsuario.add(controller.getSimpleName() + "." + handler.getMethod().getName());
		});

		assertThat(semUsuario).as("rotas autenticadas sem @UsuarioAtual").isEmpty();
	}

	@Test
	void authExpoeCadastroProtegidoEOperacoesPublicasEsperadas() {
		Set<String> rotas = new TreeSet<>();
		mappings.getHandlerMethods().forEach((mapping, handler) -> {
			if (handler.getBeanType() == AuthController.class) {
				mapping.getPatternValues().forEach(rotas::add);
			}
		});

		assertThat(rotas).containsExactlyInAnyOrder("/api/v1/auth/cadastro", "/api/v1/auth/login",
				"/api/v1/auth/refresh");
	}

	@Test
	void cadastroAnonimoEhRecusadoAntesDaValidacaoDoCorpo() throws Exception {
		mockMvc.perform(post("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());
	}
}
