package com.financeiro.application.ports.in;

import com.financeiro.domain.model.CompraParcelada;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CompraParceladaUseCase {
	CompraParcelada criar(Long usuarioId, CriarCommand command);

	List<CompraParcelada> listar(Long usuarioId);

	Pagina<CompraParcelada> listar(Long usuarioId, Paginacao paginacao);

	CompraParcelada buscar(Long usuarioId, Long compraId);

	CompraParcelada cancelar(Long usuarioId, Long compraId);

	record CriarCommand(String descricao, BigDecimal valorTotal, int numeroParcelas, LocalDate dataCompra,
			Long categoriaId, Long contaId) {
	}
}
