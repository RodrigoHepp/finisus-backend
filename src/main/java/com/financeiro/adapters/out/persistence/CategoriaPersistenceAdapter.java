package com.financeiro.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.financeiro.adapters.out.persistence.entity.CategoriaJpaEntity;
import com.financeiro.adapters.out.persistence.repository.CategoriaJpaRepository;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.CategoriaRepositoryPort;
import com.financeiro.domain.model.Categoria;

@Component
public class CategoriaPersistenceAdapter implements CategoriaRepositoryPort {
	private final CategoriaJpaRepository repository;

	public CategoriaPersistenceAdapter(CategoriaJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Categoria salvar(Categoria categoria) {
		CategoriaJpaEntity e = new CategoriaJpaEntity();
		e.setId(categoria.getId());
		e.setUsuarioId(categoria.getUsuarioId());
		e.setNome(categoria.getNome());
		e.setCategoriaPaiId(categoria.getCategoriaPaiId());
		e.setAtivo(categoria.isAtivo());
		return toDomain(repository.save(e));
	}

	@Override
	public Optional<Categoria> buscarPorId(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Categoria> buscarPorIdEUsuario(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public List<Categoria> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioIdAndAtivoTrue(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<Categoria> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository.findByUsuarioIdAndAtivoTrue(usuarioId,
						PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))),
				this::toDomain);
	}

	private Categoria toDomain(CategoriaJpaEntity e) {
		return Categoria.reconstituir(e.getId(), e.getUsuarioId(), e.getNome(), e.getCategoriaPaiId(), e.isAtivo());
	}
}
