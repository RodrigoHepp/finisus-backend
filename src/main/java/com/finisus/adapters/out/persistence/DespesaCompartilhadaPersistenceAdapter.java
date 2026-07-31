package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.DespesaCompartilhadaJpaEntity;
import com.finisus.adapters.out.persistence.repository.DespesaCompartilhadaJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.DespesaCompartilhadaRepositoryPort;
import com.finisus.domain.model.DespesaCompartilhada;
import com.finisus.domain.model.TipoAlvoCompartilhamento;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class DespesaCompartilhadaPersistenceAdapter implements DespesaCompartilhadaRepositoryPort {
	private final DespesaCompartilhadaJpaRepository repository;

	public DespesaCompartilhadaPersistenceAdapter(DespesaCompartilhadaJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public DespesaCompartilhada salvar(DespesaCompartilhada despesa) {
		DespesaCompartilhadaJpaEntity entity = despesa.getId() == null ? new DespesaCompartilhadaJpaEntity()
				: repository.findById(despesa.getId()).orElseThrow();
		entity.setTransacaoId(despesa.getTransacaoId());
		entity.setTransacaoItemId(despesa.getTransacaoItemId());
		entity.setCriadorId(despesa.getCriadorId());
		entity.setTipoRateio(despesa.getTipoRateio());
		entity.setTipoAlvo(despesa.getTipoAlvo());
		entity.setStatus(despesa.getStatus());
		entity.setCanceladaEm(despesa.getCanceladaEm());
		return toDomain(repository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<DespesaCompartilhada> buscarPorId(Long despesaId) {
		return repository.findById(despesaId).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<DespesaCompartilhada> buscarPorIdECriadorId(Long despesaId, Long criadorId) {
		return repository.findByIdAndCriadorId(despesaId, criadorId).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existePorTransacaoETipoAlvo(Long transacaoId, TipoAlvoCompartilhamento tipoAlvo) {
		return repository.existsByTransacaoIdAndTipoAlvoAndStatus(transacaoId, tipoAlvo,
				com.finisus.domain.model.StatusDespesaCompartilhada.ATIVA);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existePorTransacaoItemId(Long transacaoItemId) {
		return repository.existsByTransacaoItemIdAndStatus(transacaoItemId,
				com.finisus.domain.model.StatusDespesaCompartilhada.ATIVA);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DespesaCompartilhada> listarPorCriadorId(Long criadorId) {
		return repository.findByCriadorId(criadorId).stream().map(this::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<DespesaCompartilhada> listarPorCriadorId(Long criadorId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository.findByCriadorId(criadorId, PaginaJpaMapper.pageable(paginacao, Sort.by("id").descending())),
				this::toDomain);
	}

	private DespesaCompartilhada toDomain(DespesaCompartilhadaJpaEntity entity) {
		return DespesaCompartilhada.reconstituir(entity.getId(), entity.getTransacaoId(), entity.getTransacaoItemId(),
				entity.getCriadorId(), entity.getTipoRateio(), entity.getTipoAlvo(), entity.getStatus(),
				entity.getCanceladaEm());
	}
}
