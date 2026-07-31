package com.financeiro.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.financeiro.adapters.out.persistence.entity.FaturaJpaEntity;
import com.financeiro.adapters.out.persistence.repository.FaturaJpaRepository;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.FaturaRepositoryPort;
import com.financeiro.domain.model.Fatura;
import com.financeiro.domain.vo.AnoMes;

@Component
public class FaturaPersistenceAdapter implements FaturaRepositoryPort {
	private final FaturaJpaRepository repository;

	public FaturaPersistenceAdapter(FaturaJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Fatura salvar(Fatura f) {
		FaturaJpaEntity e = new FaturaJpaEntity();
		e.setId(f.getId());
		e.setCartaoId(f.getCartaoId());
		e.setAnoMes(f.getMesReferencia().formatado());
		e.setDataFechamento(f.getDataFechamento());
		e.setDataVencimento(f.getDataVencimento());
		e.setStatus(f.getStatus());
		e.setContaPagamentoId(f.getContaPagamentoId());
		e.setCanceladaEm(f.getCanceladaEm());
		e.setVersion(f.getVersion());
		return toDomain(repository.save(e));
	}

	@Override
	public Optional<Fatura> buscarPorCartaoEMes(Long c, AnoMes m) {
		return repository.findByCartaoIdAndAnoMes(c, m.formatado()).map(this::toDomain);
	}

	@Override
	public Optional<Fatura> buscarPorId(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Fatura> buscarPorIdParaAtualizacao(Long id) {
		return repository.findByIdForUpdate(id).map(this::toDomain);
	}

	@Override
	public List<Fatura> listarPorCartao(Long id) {
		return repository.findByCartaoId(id).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<Fatura> listarPorCartao(Long id, Paginacao p) {
		return PaginaJpaMapper.map(
				repository.findByCartaoId(id,
						PaginaJpaMapper.pageable(p, Sort.by("anoMes").descending().and(Sort.by("id").descending()))),
				this::toDomain);
	}

	private Fatura toDomain(FaturaJpaEntity e) {
		return Fatura.reconstituir(e.getId(), e.getCartaoId(), AnoMes.parse(e.getAnoMes()), e.getDataFechamento(),
				e.getDataVencimento(), e.getStatus(), e.getContaPagamentoId(),
				e.getVersion() == null ? 0 : e.getVersion(), e.getCanceladaEm());
	}
}
