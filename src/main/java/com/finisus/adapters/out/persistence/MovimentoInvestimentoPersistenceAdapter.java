package com.finisus.adapters.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.MovimentoInvestimentoJpaEntity;
import com.finisus.adapters.out.persistence.repository.MovimentoInvestimentoJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.MovimentoInvestimentoRepositoryPort;
import com.finisus.domain.model.MovimentoInvestimento;
import com.finisus.domain.vo.ValorMonetario;

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
	public Map<Long, List<MovimentoInvestimento>> listarPorInvestimentos(List<Long> investimentosIds) {
		if (investimentosIds.isEmpty()) return Map.of();
		return repository.findByInvestimentoIdInOrderByInvestimentoIdAscDataDescIdDesc(investimentosIds).stream()
				.map(this::toDomain).collect(Collectors.groupingBy(MovimentoInvestimento::getInvestimentoId));
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
