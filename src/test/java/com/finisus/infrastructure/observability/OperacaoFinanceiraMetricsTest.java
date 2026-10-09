package com.finisus.infrastructure.observability;

import com.finisus.domain.ConflitoAtualizacaoException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperacaoFinanceiraMetricsTest {
	private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
	private final OperacaoFinanceiraMetrics metrics = new OperacaoFinanceiraMetrics(registry);

	@Test
	void distingueConflitoDeFalhaGenericaSemAdicionarIdentificadores() {
		assertThatThrownBy(() -> metrics.medir("pagamento", () -> {
			throw new ConflitoAtualizacaoException();
		})).isInstanceOf(ConflitoAtualizacaoException.class);

		var contador = registry.get("financeiro.operacoes")
				.tags("operacao", "pagamento", "resultado", "conflito").counter();
		assertThat(contador.count()).isEqualTo(1);
		assertThat(contador.getId().getTags()).allMatch(tag -> !tag.getKey().contains("usuario"));
	}

	@Test
	void registraDivergenciaDeSaldoComTagDeBaixaCardinalidade() {
		metrics.registrarDivergenciaSaldo();

		var contador = registry.get("financeiro.eventos").tag("evento", "divergencia_saldo").counter();
		assertThat(contador.count()).isEqualTo(1);
		assertThat(contador.getId().getTags()).hasSize(1);
	}

	@Test
	void registraDuplicidadeEvitadaSemIdentificadores() {
		metrics.registrar("duplicidade_importacao_evitada");

		var contador = registry.get("financeiro.eventos").tag("evento", "duplicidade_importacao_evitada").counter();
		assertThat(contador.count()).isEqualTo(1);
		assertThat(contador.getId().getTags()).hasSize(1);
	}
}
