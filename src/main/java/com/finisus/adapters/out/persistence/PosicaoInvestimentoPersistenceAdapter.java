package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.PosicaoInvestimentoJpaEntity;
import com.finisus.adapters.out.persistence.repository.PosicaoInvestimentoJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.PosicaoInvestimentoRepositoryPort;
import com.finisus.domain.ConflitoAtualizacaoException;
import com.finisus.domain.model.PosicaoInvestimento;
import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
public class PosicaoInvestimentoPersistenceAdapter implements PosicaoInvestimentoRepositoryPort {
	private final PosicaoInvestimentoJpaRepository repository;

	public PosicaoInvestimentoPersistenceAdapter(PosicaoInvestimentoJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public PosicaoInvestimento salvar(PosicaoInvestimento posicao) {
		PosicaoInvestimentoJpaEntity entity = new PosicaoInvestimentoJpaEntity();
		entity.setId(posicao.getId());
		entity.setInvestimentoId(posicao.getInvestimentoId());
		entity.setValor(posicao.getValor().valor());
		entity.setDataReferencia(posicao.getDataReferencia());
		entity.setVersion(posicao.getVersion());
		try {
			return toDomain(repository.saveAndFlush(entity));
		} catch (ObjectOptimisticLockingFailureException exception) {
			throw new ConflitoAtualizacaoException();
		}
	}

	@Override
	public Pagina<PosicaoInvestimento> listarPorInvestimento(Long investimentoId, Paginacao paginacao) {
		return PaginaJpaMapper.map(repository.findByInvestimentoId(investimentoId,
				PaginaJpaMapper.pageable(paginacao,
						Sort.by("dataReferencia").descending().and(Sort.by("id").descending()))), this::toDomain);
	}

	@Override
	public Optional<PosicaoInvestimento> buscarUltimaAte(Long investimentoId, LocalDate referencia) {
		return repository
				.findFirstByInvestimentoIdAndDataReferenciaLessThanEqualOrderByDataReferenciaDesc(investimentoId, referencia)
				.map(this::toDomain);
	}

	@Override
	public Map<Long, PosicaoInvestimento> buscarUltimasAte(List<Long> investimentosIds, LocalDate referencia) {
		if (investimentosIds.isEmpty()) return Map.of();
		return repository.findUltimasAte(investimentosIds, referencia).stream().map(this::toDomain)
				.collect(Collectors.toMap(PosicaoInvestimento::getInvestimentoId, Function.identity()));
	}

	private PosicaoInvestimento toDomain(PosicaoInvestimentoJpaEntity e) {
		return PosicaoInvestimento.reconstituir(e.getId(), e.getInvestimentoId(), ValorMonetario.of(e.getValor()),
				e.getDataReferencia(), e.getVersion() == null ? 0 : e.getVersion());
	}
}
