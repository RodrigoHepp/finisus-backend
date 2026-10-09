package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.PosicaoInvestimento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface PosicaoInvestimentoUseCase {
	PosicaoInvestimento registrar(Long usuarioId, Long investimentoId, RegistrarCommand command);
	Pagina<PosicaoInvestimento> listar(Long usuarioId, Long investimentoId, Paginacao paginacao);
	Optional<PosicaoInvestimento> buscarUltima(Long usuarioId, Long investimentoId, LocalDate referencia);

	record RegistrarCommand(BigDecimal valor, LocalDate dataReferencia) { }
}
