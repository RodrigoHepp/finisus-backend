package com.finisus.adapters.out.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.AplicacaoCreditoFaturaJpaEntity;
import com.finisus.adapters.out.persistence.repository.AplicacaoCreditoFaturaJpaRepository;
import com.finisus.application.ports.out.AplicacaoCreditoFaturaRepositoryPort;
import com.finisus.domain.model.AplicacaoCreditoFatura;
import com.finisus.domain.vo.ValorMonetario;

@Component
public class AplicacaoCreditoFaturaPersistenceAdapter implements AplicacaoCreditoFaturaRepositoryPort {
	private final AplicacaoCreditoFaturaJpaRepository repository;
	public AplicacaoCreditoFaturaPersistenceAdapter(AplicacaoCreditoFaturaJpaRepository repository) { this.repository = repository; }
	@Override public AplicacaoCreditoFatura salvar(AplicacaoCreditoFatura a) {
		var e = new AplicacaoCreditoFaturaJpaEntity(); e.setId(a.id()); e.setPagamentoOrigemId(a.pagamentoOrigemId());
		e.setFaturaDestinoId(a.faturaDestinoId()); e.setValor(a.valor().valor()); e.setCriadaEm(a.criadaEm());
		var salvo = repository.saveAndFlush(e);
		return new AplicacaoCreditoFatura(salvo.getId(), salvo.getPagamentoOrigemId(), salvo.getFaturaDestinoId(),
				ValorMonetario.of(salvo.getValor()), salvo.getCriadaEm());
	}
	@Override public BigDecimal somarPorPagamentoOrigem(Long id) { return repository.sumByPagamentoOrigemId(id); }
	@Override public BigDecimal somarPorFaturaDestino(Long id) { return repository.sumByFaturaDestinoId(id); }
	@Override public Map<Long, BigDecimal> somarPorFaturasDestino(List<Long> faturasIds) {
		if (faturasIds.isEmpty()) return Map.of();
		return repository.sumByFaturaDestinoIdIn(faturasIds).stream()
				.collect(Collectors.toMap(linha -> (Long) linha[0], linha -> (BigDecimal) linha[1]));
	}
	@Override public boolean existePorPagamentoOrigem(Long id) { return repository.existsByPagamentoOrigemId(id); }
}
