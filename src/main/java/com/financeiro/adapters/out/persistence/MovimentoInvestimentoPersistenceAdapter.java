package com.financeiro.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.financeiro.adapters.out.persistence.entity.MovimentoInvestimentoJpaEntity;
import com.financeiro.adapters.out.persistence.repository.MovimentoInvestimentoJpaRepository;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.MovimentoInvestimentoRepositoryPort;
import com.financeiro.domain.model.MovimentoInvestimento;
import com.financeiro.domain.vo.ValorMonetario;

@Component
public class MovimentoInvestimentoPersistenceAdapter implements MovimentoInvestimentoRepositoryPort {
	private final MovimentoInvestimentoJpaRepository repository;

	public MovimentoInvestimentoPersistenceAdapter(MovimentoInvestimentoJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public MovimentoInvestimento salvar(MovimentoInvestimento movimento) {
		MovimentoInvestimentoJpaEntity entity = new MovimentoInvestimentoJpaEntity();
		entity.setId(movimento.getId());
		entity.setInvestimentoId(movimento.getInvestimentoId());
		entity.setTipo(movimento.getTipo());
		entity.setValor(movimento.getValor().valor());
		entity.setData(movimento.getData());
		entity.setTransacaoId(movimento.getTransacaoId());
		entity.setMovimentoOrigemId(movimento.getMovimentoOrigemId());
		entity.setEstornadoEm(movimento.getEstornadoEm());
		return toDomain(repository.save(entity));
	}

	@Override
	public List<MovimentoInvestimento> listarPorInvestimento(Long investimentoId) {
		return repository.findByInvestimentoIdOrderByDataDesc(investimentoId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<MovimentoInvestimento> listarPorInvestimento(Long investimentoId, Paginacao paginacao) {
		return PaginaJpaMapper.map(repository.findByInvestimentoId(investimentoId,
				PaginaJpaMapper.pageable(paginacao, Sort.by("data").descending().and(Sort.by("id").descending()))),
				this::toDomain);
	}

	@Override
	public Optional<MovimentoInvestimento> buscarPorIdEInvestimento(Long movimentoId, Long investimentoId) {
		return repository.findByIdAndInvestimentoId(movimentoId, investimentoId).map(this::toDomain);
	}

	@Override
	public Optional<MovimentoInvestimento> buscarPorId(Long movimentoId) {
		return repository.findById(movimentoId).map(this::toDomain);
	}

	@Override
	public boolean existeCompensacao(Long movimentoOrigemId) {
		return repository.existsByMovimentoOrigemId(movimentoOrigemId);
	}

	private MovimentoInvestimento toDomain(MovimentoInvestimentoJpaEntity entity) {
		return MovimentoInvestimento.reconstituir(entity.getId(), entity.getInvestimentoId(), entity.getTipo(),
				ValorMonetario.of(entity.getValor()), entity.getData(), entity.getTransacaoId(),
				entity.getMovimentoOrigemId(), entity.getEstornadoEm());
	}
}
