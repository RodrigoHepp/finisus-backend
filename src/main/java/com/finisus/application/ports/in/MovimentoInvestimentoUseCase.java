package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.MovimentoInvestimento;
import com.finisus.domain.model.TipoMovimentoInvestimento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MovimentoInvestimentoUseCase {
	MovimentoInvestimento movimentar(Long usuarioId, MovimentoCommand command);

	List<MovimentoInvestimento> listar(Long usuarioId, Long investimentoId);

	Pagina<MovimentoInvestimento> listar(Long usuarioId, Long investimentoId, Paginacao paginacao);

	MovimentoInvestimento estornar(Long usuarioId, Long movimentoId);

	record MovimentoCommand(Long investimentoId, TipoMovimentoInvestimento tipo, BigDecimal valor, LocalDate data) {
	}
}
