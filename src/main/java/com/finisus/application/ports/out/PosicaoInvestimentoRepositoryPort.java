package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.PosicaoInvestimento;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PosicaoInvestimentoRepositoryPort {
	PosicaoInvestimento salvar(PosicaoInvestimento posicao);
	Pagina<PosicaoInvestimento> listarPorInvestimento(Long investimentoId, Paginacao paginacao);
	Optional<PosicaoInvestimento> buscarUltimaAte(Long investimentoId, LocalDate referencia);
	Map<Long, PosicaoInvestimento> buscarUltimasAte(List<Long> investimentosIds, LocalDate referencia);
}
