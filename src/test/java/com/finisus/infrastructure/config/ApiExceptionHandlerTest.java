package com.finisus.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import com.finisus.domain.ConflitoAtualizacaoException;
import com.finisus.domain.DomainException;

class ApiExceptionHandlerTest {

	@Test
	void publicaCodigoEstavelDaExcecaoDeDominio() {
		StaticMessageSource messages = new StaticMessageSource();
		messages.addMessage("error.recurso.nao.encontrado", Locale.getDefault(), "Recurso não encontrado.");
		var request = new MockHttpServletRequest("GET", "/api/v1/transacoes/99");

		var problem = new ApiExceptionHandler(messages)
				.handleDomain(new DomainException("error.recurso.nao.encontrado"), request);

		assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
		assertThat(problem.getType()).hasToString("urn:finisus:problem:error.recurso.nao.encontrado");
		assertThat(problem.getProperties()).containsEntry("code", "error.recurso.nao.encontrado");
		assertThat(problem.getDetail()).isEqualTo("Recurso não encontrado.");
	}

	@Test
	void publicaMesmoCodigoParaConflitoDeDominioEInfraestrutura() {
		StaticMessageSource messages = new StaticMessageSource();
		messages.addMessage("error.conflito.atualizacao", Locale.getDefault(), "Conflito.");
		var request = new MockHttpServletRequest("PATCH", "/api/v1/transacoes/1");
		ApiExceptionHandler handler = new ApiExceptionHandler(messages);

		var dominio = handler.handleConflict(new ConflitoAtualizacaoException(), request);
		var infraestrutura = handler.handleConcurrency(new org.springframework.dao.CannotAcquireLockException("lock"),
				request);

		assertThat(dominio.getProperties()).containsEntry("code", "error.conflito.atualizacao");
		assertThat(infraestrutura.getProperties()).containsEntry("code", "error.conflito.atualizacao");
		assertThat(dominio.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
		assertThat(infraestrutura.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
	}
}
