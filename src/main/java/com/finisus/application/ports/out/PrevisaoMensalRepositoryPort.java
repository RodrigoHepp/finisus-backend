package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.PrevisaoMensal;
import com.finisus.domain.vo.AnoMes;

import java.util.List;

public interface PrevisaoMensalRepositoryPort {
	void substituir(Long usuarioId, AnoMes anoMes, List<PrevisaoMensal> previsoes);

	List<PrevisaoMensal> listar(Long usuarioId, AnoMes anoMes);

	Pagina<PrevisaoMensal> listar(Long usuarioId, AnoMes anoMes, Paginacao paginacao);
}
