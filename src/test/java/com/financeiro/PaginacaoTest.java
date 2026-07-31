package com.financeiro;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginacaoTest {
	@Test
	void aceitaTamanhoMaximo() {
		assertThat(new Paginacao(0, 100).tamanho()).isEqualTo(100);
	}

	@Test
	void rejeitaPaginaNegativaETamanhoAcimaDoLimite() {
		assertThatThrownBy(() -> new Paginacao(-1, 20)).isInstanceOf(DomainException.class);
		assertThatThrownBy(() -> new Paginacao(0, 101)).isInstanceOf(DomainException.class);
	}
}
