package com.financeiro.application.ports.in;

import com.financeiro.domain.model.Recorrencia;
import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.model.Transacao;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import java.math.BigDecimal;
import java.util.List;

public interface RecorrenciaUseCase extends GerarRecorrenciasMensaisUseCase {
	Recorrencia criar(Long usuarioId, CriarCommand command);

	List<Recorrencia> listar(Long usuarioId);

	Pagina<Recorrencia> listar(Long usuarioId, Paginacao paginacao);

	Recorrencia buscar(Long usuarioId, Long recorrenciaId);

	Recorrencia atualizar(Long usuarioId, Long recorrenciaId, CriarCommand command);

	Recorrencia inativar(Long usuarioId, Long recorrenciaId);

	record CriarCommand(String nome, TipoTransacao tipo, BigDecimal valorEsperado, int diaDoMes, Long categoriaId,
			Long contaId, Long meioPagamentoId) {
	}
}
