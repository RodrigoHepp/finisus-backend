package com.finisus.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.FinanciamentoJpaEntity;
import com.finisus.adapters.out.persistence.repository.FinanciamentoJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.FinanciamentoRepositoryPort;
import com.finisus.domain.model.Financiamento;
import com.finisus.domain.vo.ValorMonetario;

@Component
public class FinanciamentoPersistenceAdapter implements FinanciamentoRepositoryPort {
	private final FinanciamentoJpaRepository repository;

	public FinanciamentoPersistenceAdapter(FinanciamentoJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Financiamento salvar(Financiamento financiamento) {
		FinanciamentoJpaEntity entity = financiamento.getId() == null ? new FinanciamentoJpaEntity()
				: repository.findById(financiamento.getId()).orElseThrow();
		entity.setUsuarioId(financiamento.getUsuarioId());
		entity.setDescricao(financiamento.getDescricao());
		entity.setPrincipal(financiamento.getPrincipal().valor());
		entity.setTaxaJurosMensal(financiamento.getTaxaJurosMensal());
		entity.setNumeroParcelas(financiamento.getNumeroParcelas());
		entity.setDataInicio(financiamento.getDataInicio());
		entity.setContaId(financiamento.getContaId());
		entity.setFinalizadoEm(financiamento.getFinalizadoEm());
		entity.setStatus(financiamento.getStatus());
		entity.setCanceladaEm(financiamento.getCanceladaEm());
		return toDomain(repository.save(entity));
	}

	@Override
	public Optional<Financiamento> buscarPorIdEUsuario(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public List<Financiamento> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<Financiamento> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository
						.findByUsuarioId(usuarioId,
								PaginaJpaMapper.pageable(paginacao,
										Sort.by("dataInicio").descending().and(Sort.by("id").descending()))),
				this::toDomain);
	}

	private Financiamento toDomain(FinanciamentoJpaEntity entity) {
		return Financiamento.reconstituir(entity.getId(), entity.getUsuarioId(), entity.getDescricao(),
				ValorMonetario.of(entity.getPrincipal()), entity.getTaxaJurosMensal(), entity.getNumeroParcelas(),
				entity.getDataInicio(), entity.getContaId(), entity.getFinalizadoEm(), entity.getStatus(),
				entity.getCanceladaEm());
	}
}
