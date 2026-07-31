package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.PrevisaoMensal;

import java.util.List;

public interface PrevisaoFluxoCaixaUseCase {
	List<PrevisaoMensal> recalcular(Long usuarioId, int meses);

	List<PrevisaoMensal> consultar(Long usuarioId, String anoMes);

	Pagina<PrevisaoMensal> consultar(Long usuarioId, String anoMes, Paginacao paginacao);
}
