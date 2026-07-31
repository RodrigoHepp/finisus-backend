package com.finisus.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.CompraParceladaJpaEntity;
import com.finisus.adapters.out.persistence.repository.CompraParceladaJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.CompraParceladaRepositoryPort;
import com.finisus.domain.model.CompraParcelada;
import com.finisus.domain.vo.ValorMonetario;

@Component
public class CompraParceladaPersistenceAdapter implements CompraParceladaRepositoryPort {
	private final CompraParceladaJpaRepository repository;

	public CompraParceladaPersistenceAdapter(CompraParceladaJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public CompraParcelada salvar(CompraParcelada compra) {
		CompraParceladaJpaEntity e = new CompraParceladaJpaEntity();
		e.setId(compra.getId());
		e.setUsuarioId(compra.getUsuarioId());
		e.setDescricao(compra.getDescricao());
		e.setValorTotal(compra.getValorTotal().valor());
		e.setNumeroParcelas(compra.getNumeroParcelas());
		e.setDataCompra(compra.getDataCompra());
		e.setCategoriaId(compra.getCategoriaId());
		e.setContaId(compra.getContaId());
		e.setCanceladaEm(compra.getCanceladaEm());
		return toDomain(repository.save(e));
	}

	@Override
	public Optional<CompraParcelada> buscarPorId(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<CompraParcelada> buscarPorIdEUsuario(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public List<CompraParcelada> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<CompraParcelada> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository
						.findByUsuarioId(usuarioId,
								PaginaJpaMapper.pageable(paginacao,
										Sort.by("dataCompra").descending().and(Sort.by("id").descending()))),
				this::toDomain);
	}

	private CompraParcelada toDomain(CompraParceladaJpaEntity e) {
		return CompraParcelada.reconstituir(e.getId(), e.getUsuarioId(), e.getDescricao(),
				ValorMonetario.of(e.getValorTotal()), e.getNumeroParcelas(), e.getDataCompra(), e.getCategoriaId(),
				e.getContaId(), e.getCanceladaEm());
	}
}
