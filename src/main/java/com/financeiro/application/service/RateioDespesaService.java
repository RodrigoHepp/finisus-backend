package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.RateioDespesaUseCase;
import com.financeiro.application.ports.out.DespesaCompartilhadaRepositoryPort;
import com.financeiro.application.ports.out.RateioDespesaRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.RateioDespesa;
import com.financeiro.domain.model.TipoParticipante;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class RateioDespesaService implements RateioDespesaUseCase {
	private final RateioDespesaRepositoryPort rateios;
	private final DespesaCompartilhadaRepositoryPort despesas;

	public RateioDespesaService(RateioDespesaRepositoryPort rateios, DespesaCompartilhadaRepositoryPort despesas) {
		this.rateios = rateios;
		this.despesas = despesas;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RateioDespesa> listar(Long usuarioId, Long despesaId) {
		exigir(despesas.buscarPorIdECriadorId(despesaId, usuarioId));
		return rateios.listarPorDespesaId(despesaId);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<RateioDespesa> listar(Long usuarioId, Long despesaId, Paginacao paginacao) {
		exigir(despesas.buscarPorIdECriadorId(despesaId, usuarioId));
		return rateios.listarPorDespesaId(despesaId, paginacao);
	}

	@Override
	@Transactional
	public RateioDespesa responder(Long usuarioId, Long rateioId, boolean aceita) {
		RateioDespesa rateio = exigir(rateios.buscarPorId(rateioId));
		if (rateio.getTipoParticipante() != TipoParticipante.INTERNO || !usuarioId.equals(rateio.getUsuarioId())) {
			throw new DomainException("error.recurso.nao.encontrado");
		}
		if (aceita)
			rateio.aceitar();
		else
			rateio.recusar();
		return rateios.salvar(rateio);
	}

	@Override
	@Transactional
	public RateioDespesa marcarPago(Long usuarioId, Long rateioId) {
		RateioDespesa rateio = exigir(rateios.buscarPorId(rateioId));
		exigir(despesas.buscarPorIdECriadorId(rateio.getDespesaCompartilhadaId(), usuarioId));
		rateio.pagar();
		return rateios.salvar(rateio);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<RateioRecebido> listarRecebidos(Long usuarioId, Paginacao paginacao) {
		return rateios.listarRecebidosPorUsuarioId(usuarioId, paginacao).map(
				rateio -> new RateioRecebido(rateio, exigir(despesas.buscarPorId(rateio.getDespesaCompartilhadaId()))));
	}

	private <T> T exigir(java.util.Optional<T> value) {
		return value.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}
}
