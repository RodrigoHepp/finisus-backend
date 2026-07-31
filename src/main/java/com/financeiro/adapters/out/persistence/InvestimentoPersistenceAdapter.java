package com.financeiro.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.financeiro.adapters.out.persistence.entity.InvestimentoJpaEntity;
import com.financeiro.adapters.out.persistence.repository.InvestimentoJpaRepository;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.InvestimentoRepositoryPort;
import com.financeiro.domain.model.Investimento;

@Component
public class InvestimentoPersistenceAdapter implements InvestimentoRepositoryPort {
	private final InvestimentoJpaRepository repository;

	public InvestimentoPersistenceAdapter(InvestimentoJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Investimento salvar(Investimento investimento) {
		InvestimentoJpaEntity entity = new InvestimentoJpaEntity();
		entity.setId(investimento.getId());
		entity.setUsuarioId(investimento.getUsuarioId());
		entity.setNome(investimento.getNome());
		entity.setTipo(investimento.getTipo());
		entity.setContaOrigemId(investimento.getContaOrigemId());
		entity.setAtivo(investimento.isAtivo());
		return toDomain(repository.save(entity));
	}

	@Override
	public Optional<Investimento> buscarPorIdEUsuario(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public List<Investimento> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<Investimento> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository.findByUsuarioId(usuarioId,
						PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))),
				this::toDomain);
	}

	private Investimento toDomain(InvestimentoJpaEntity entity) {
		return Investimento.reconstituir(entity.getId(), entity.getUsuarioId(), entity.getNome(), entity.getTipo(),
				entity.getContaOrigemId(), entity.isAtivo());
	}
}
