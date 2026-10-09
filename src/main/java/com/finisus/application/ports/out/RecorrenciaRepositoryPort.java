package com.finisus.application.ports.out;

import com.finisus.domain.model.Recorrencia;
import com.finisus.domain.model.OcorrenciaRecorrencia;
import com.finisus.domain.vo.AnoMes;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RecorrenciaRepositoryPort {

	Recorrencia salvar(Recorrencia recorrencia);

	Optional<Recorrencia> buscarPorId(Long id);

	Optional<Recorrencia> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<Recorrencia> listarPorUsuario(Long usuarioId);

	Pagina<Recorrencia> listarPorUsuario(Long usuarioId, Paginacao paginacao);

	List<Recorrencia> listarAtivasPorUsuario(Long usuarioId);

	OcorrenciaRecorrencia salvarOcorrencia(OcorrenciaRecorrencia ocorrencia);

	Optional<OcorrenciaRecorrencia> buscarOcorrenciaParaAtualizacao(Long id, Long usuarioId);

	List<OcorrenciaRecorrencia> listarOcorrencias(Long usuarioId, AnoMes anoMes);

	boolean existsGeracaoPorRecorrenciaEAnoMes(Long recorrenciaId, AnoMes anoMes);

	Set<ChaveGeracao> listarGeracoesExistentes(List<Long> recorrenciasIds, AnoMes inicio, AnoMes fim);

	record ChaveGeracao(Long recorrenciaId, AnoMes anoMes) {
	}
}
