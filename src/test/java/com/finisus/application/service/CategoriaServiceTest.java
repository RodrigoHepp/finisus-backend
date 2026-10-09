package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.finisus.application.ports.in.CategoriaUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Categoria;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CategoriaServiceTest {
	@Test
	void recusaCicloIndiretoAoMoverCategoriaParaUmaDescendente() {
		CategoriaRepositoryPort categorias = mock(CategoriaRepositoryPort.class);
		Categoria a = Categoria.reconstituir(1L, 10L, "A", null, true);
		Categoria b = Categoria.reconstituir(2L, 10L, "B", 1L, true);
		Categoria c = Categoria.reconstituir(3L, 10L, "C", 2L, true);
		when(categorias.buscarPorIdEUsuario(1L, 10L)).thenReturn(Optional.of(a));
		when(categorias.listarTodasPorUsuarioParaAtualizacao(10L)).thenReturn(List.of(a, b, c));

		assertThatThrownBy(() -> new CategoriaService(categorias).atualizar(10L, 1L,
				new CategoriaUseCase.CriarCommand("A", 3L)))
				.isInstanceOf(DomainException.class).hasMessage("error.categoria.pai.invalida");
	}

	@Test
	void recusaCategoriaPaiInativa() {
		CategoriaRepositoryPort categorias = mock(CategoriaRepositoryPort.class);
		Categoria pai = Categoria.reconstituir(1L, 10L, "Pai", null, false);
		when(categorias.listarTodasPorUsuarioParaAtualizacao(10L)).thenReturn(List.of(pai));

		assertThatThrownBy(() -> new CategoriaService(categorias).criar(10L,
				new CategoriaUseCase.CriarCommand("Filha", 1L)))
				.isInstanceOf(DomainException.class).hasMessage("error.categoria.pai.inativa");
	}

	@Test
	void recusaSextoNivelDaHierarquia() {
		CategoriaRepositoryPort categorias = mock(CategoriaRepositoryPort.class);
		Categoria nivel1 = Categoria.reconstituir(1L, 10L, "N1", null, true);
		Categoria nivel2 = Categoria.reconstituir(2L, 10L, "N2", 1L, true);
		Categoria nivel3 = Categoria.reconstituir(3L, 10L, "N3", 2L, true);
		Categoria nivel4 = Categoria.reconstituir(4L, 10L, "N4", 3L, true);
		Categoria nivel5 = Categoria.reconstituir(5L, 10L, "N5", 4L, true);
		when(categorias.listarTodasPorUsuarioParaAtualizacao(10L))
				.thenReturn(List.of(nivel1, nivel2, nivel3, nivel4, nivel5));

		assertThatThrownBy(() -> new CategoriaService(categorias).criar(10L,
				new CategoriaUseCase.CriarCommand("N6", 5L)))
				.isInstanceOf(DomainException.class).hasMessage("error.categoria.profundidade.excedida");
	}

	@Test
	void exigeInativacaoDasDescendentesAntesDaCategoriaPai() {
		CategoriaRepositoryPort categorias = mock(CategoriaRepositoryPort.class);
		Categoria pai = Categoria.reconstituir(1L, 10L, "Pai", null, true);
		Categoria filha = Categoria.reconstituir(2L, 10L, "Filha", 1L, true);
		Categoria neta = Categoria.reconstituir(3L, 10L, "Neta", 2L, true);
		when(categorias.listarTodasPorUsuarioParaAtualizacao(10L)).thenReturn(List.of(pai, filha, neta));

		assertThatThrownBy(() -> new CategoriaService(categorias).inativar(10L, 1L))
				.isInstanceOf(DomainException.class).hasMessage("error.categoria.descendente.ativa");
	}

	@Test
	void recusaCategoriaInativaEmNovaClassificacao() {
		Categoria inativa = Categoria.reconstituir(1L, 10L, "Antiga", null, false);

		assertThatThrownBy(() -> CategoriaAtivaValidator.exigirAtiva(inativa))
				.isInstanceOf(DomainException.class).hasMessage("error.categoria.inativa");
	}
}
