package com.finisus.application.service;

import com.finisus.application.ports.in.BancoUseCase;
import com.finisus.application.ports.out.BancoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Banco;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BancoServiceTest {
	@Test
	void permiteLeituraDeBancoCompartilhadoSemTransferirOwnership() {
		BancoRepositoryPort bancos = mock(BancoRepositoryPort.class);
		Banco bancoDoSistema = Banco.reconstituir(10L, "Banco Inicial", "001", true, null);
		when(bancos.buscarPorId(10L)).thenReturn(Optional.of(bancoDoSistema));

		Banco encontrado = new BancoService(bancos).buscar(1L, 10L);

		assertThat(encontrado).isSameAs(bancoDoSistema);
		assertThat(encontrado.isSistema()).isTrue();
		assertThat(encontrado.getUsuarioId()).isNull();
		verify(bancos, never()).salvar(any());
	}

	@Test
	void recusaAtualizacaoDeBancoCompartilhado() {
		BancoRepositoryPort bancos = mock(BancoRepositoryPort.class);
		Banco bancoDoSistema = Banco.reconstituir(10L, "Banco Inicial", "001", true, null);
		when(bancos.buscarPorId(10L)).thenReturn(Optional.of(bancoDoSistema));
		assertThatThrownBy(() -> new BancoService(bancos).atualizar(1L, 10L,
				new BancoUseCase.CriarCommand("Banco Atualizado", "999")))
				.isInstanceOf(DomainException.class).hasMessage("error.recurso.nao.encontrado");
		verify(bancos, never()).salvar(any());
	}

	@Test
	void recusaInativacaoDeBancoCompartilhado() {
		BancoRepositoryPort bancos = mock(BancoRepositoryPort.class);
		Banco bancoDoSistema = Banco.reconstituir(10L, "Banco Inicial", "001", true, null);
		when(bancos.buscarPorId(10L)).thenReturn(Optional.of(bancoDoSistema));
		assertThatThrownBy(() -> new BancoService(bancos).inativar(1L, 10L))
				.isInstanceOf(DomainException.class).hasMessage("error.recurso.nao.encontrado");
		verify(bancos, never()).salvar(any());
	}

	@Test
	void mantemProtecaoDeBancoPertencenteAOutroUsuario() {
		BancoRepositoryPort bancos = mock(BancoRepositoryPort.class);
		Banco bancoDeOutroUsuario = Banco.reconstituir(10L, "Banco Privado", "001", true, 2L);
		when(bancos.buscarPorId(10L)).thenReturn(Optional.of(bancoDeOutroUsuario));

		assertThatThrownBy(() -> new BancoService(bancos).buscar(1L, 10L))
				.isInstanceOf(DomainException.class).hasMessage("error.recurso.nao.encontrado");
	}
}
