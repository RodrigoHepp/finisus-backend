package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.DespesaCompartilhada;
import com.finisus.domain.model.RateioDespesa;
import com.finisus.domain.model.TipoRateio;

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
