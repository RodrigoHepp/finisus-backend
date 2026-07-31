package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.DespesaCompartilhada;
import com.financeiro.domain.model.RateioDespesa;
import com.financeiro.domain.model.TipoRateio;

import java.math.BigDecimal;
import java.util.List;

public interface DespesaCompartilhadaUseCase {
	Resultado criar(Long usuarioId, CriarCommand command);

	List<DespesaCompartilhada> listar(Long usuarioId);

	Pagina<DespesaCompartilhada> listar(Long usuarioId, Paginacao paginacao);

	DespesaCompartilhada buscar(Long usuarioId, Long despesaId);

	Resultado cancelar(Long usuarioId, Long despesaId);

	record CriarCommand(Long transacaoId, Long transacaoItemId, TipoRateio tipoRateio,
			List<ParticipanteCommand> participantes) {
	}

	record ParticipanteCommand(Long usuarioId, String nomeExterno, String emailExterno, BigDecimal valorFixo,
			BigDecimal percentual) {
	}

	record Resultado(DespesaCompartilhada despesa, List<RateioDespesa> rateios) {
	}
}
