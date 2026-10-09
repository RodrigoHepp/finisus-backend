package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.CategoriaUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Categoria;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

public class CategoriaService implements CategoriaUseCase {
	private static final int PROFUNDIDADE_MAXIMA = 5;
	private final CategoriaRepositoryPort categorias;

	public CategoriaService(CategoriaRepositoryPort categorias) {
		this.categorias = categorias;
	}

	@Override
	@Transactional
	public Categoria criar(Long usuarioId, CriarCommand command) {
		validarHierarquia(usuarioId, command.categoriaPaiId(), null);
		return categorias.salvar(Categoria.nova(usuarioId, command.nome(), command.categoriaPaiId()));
	}

	@Override
	public List<Categoria> listar(Long usuarioId) {
		return categorias.listarPorUsuario(usuarioId);
	}

	@Override
	public Pagina<Categoria> listar(Long usuarioId, Paginacao paginacao) {
		return categorias.listarPorUsuario(usuarioId, paginacao);
	}

	@Override
	public Categoria buscar(Long usuarioId, Long categoriaId) {
		return categorias.buscarPorIdEUsuario(categoriaId, usuarioId).orElseThrow(this::notFound);
	}

	@Override
	@Transactional
	public Categoria atualizar(Long usuarioId, Long categoriaId, CriarCommand command) {
		Categoria atual = buscar(usuarioId, categoriaId);
		validarHierarquia(usuarioId, command.categoriaPaiId(), categoriaId);
		return categorias.salvar(Categoria.reconstituir(atual.getId(), usuarioId, command.nome(),
				command.categoriaPaiId(), atual.isAtivo()));
	}

	@Override
	@Transactional
	public Categoria inativar(Long usuarioId, Long categoriaId) {
		Map<Long, Categoria> porId = mapaBloqueado(usuarioId);
		Categoria atual = porId.get(categoriaId);
		if (atual == null)
			throw notFound();
		boolean possuiDescendenteAtiva = porId.values().stream().filter(Categoria::isAtivo)
				.anyMatch(categoria -> descendeDe(categoria, categoriaId, porId));
		if (possuiDescendenteAtiva)
			throw new DomainException("error.categoria.descendente.ativa");
		return categorias.salvar(
				Categoria.reconstituir(atual.getId(), usuarioId, atual.getNome(), atual.getCategoriaPaiId(), false));
	}

	private void validarHierarquia(Long usuarioId, Long paiId, Long categoriaId) {
		if (paiId == null)
			return;
		if (paiId.equals(categoriaId))
			throw new DomainException("error.categoria.pai.invalida");
		Map<Long, Categoria> porId = mapaBloqueado(usuarioId);
		Categoria pai = porId.get(paiId);
		if (pai == null) throw notFound();
		if (!pai.isAtivo()) throw new DomainException("error.categoria.pai.inativa");
		int profundidade = 1;
		Categoria atual = pai;
		java.util.Set<Long> visitadas = new java.util.HashSet<>();
		while (atual != null) {
			if (!visitadas.add(atual.getId()))
				throw new DomainException("error.categoria.pai.invalida");
			profundidade++;
			if (profundidade > PROFUNDIDADE_MAXIMA)
				throw new DomainException("error.categoria.profundidade.excedida");
			if (atual.getId().equals(categoriaId))
				throw new DomainException("error.categoria.pai.invalida");
			if (atual.getCategoriaPaiId() == null)
				break;
			atual = porId.get(atual.getCategoriaPaiId());
			if (atual == null)
				throw new DomainException("error.categoria.pai.invalida");
		}
	}

	private Map<Long, Categoria> mapaBloqueado(Long usuarioId) {
		return categorias.listarTodasPorUsuarioParaAtualizacao(usuarioId).stream()
				.collect(Collectors.toMap(Categoria::getId, Function.identity()));
	}

	private boolean descendeDe(Categoria categoria, Long ancestralId, Map<Long, Categoria> porId) {
		Long paiId = categoria.getCategoriaPaiId();
		java.util.Set<Long> visitadas = new java.util.HashSet<>();
		while (paiId != null) {
			if (!visitadas.add(paiId))
				return false;
			if (paiId.equals(ancestralId))
				return true;
			Categoria pai = porId.get(paiId);
			paiId = pai == null ? null : pai.getCategoriaPaiId();
		}
		return false;
	}

	private DomainException notFound() {
		return new DomainException("error.recurso.nao.encontrado");
	}
}
