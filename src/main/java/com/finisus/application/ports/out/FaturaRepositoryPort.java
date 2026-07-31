package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.vo.AnoMes;
import java.util.List;
import java.util.Optional;

public interface FaturaRepositoryPort {
	Fatura salvar(Fatura fatura);

	Optional<Fatura> buscarPorCartaoEMes(Long cartaoId, AnoMes anoMes);

	Optional<Fatura> buscarPorId(Long id);

	Optional<Fatura> buscarPorIdParaAtualizacao(Long id);

	List<Fatura> listarPorCartao(Long cartaoId);

	Pagina<Fatura> listarPorCartao(Long cartaoId, Paginacao paginacao);
}
