package com.finisus.application.ports.in;

import com.finisus.domain.model.OcorrenciaRecorrencia;

import java.util.List;

public interface GerarRecorrenciasMensaisUseCase {
	List<OcorrenciaRecorrencia> gerarMes(Long usuarioId, String anoMes);

	List<OcorrenciaRecorrencia> listarOcorrencias(Long usuarioId, String anoMes);

	OcorrenciaRecorrencia realizar(Long usuarioId, Long ocorrenciaId);
}
