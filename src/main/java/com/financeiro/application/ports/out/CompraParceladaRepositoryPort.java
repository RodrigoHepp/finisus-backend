package com.financeiro.application.ports.out;

import com.financeiro.domain.model.CompraParcelada;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;

public interface CompraParceladaRepositoryPort {

	CompraParcelada salvar(CompraParcelada compraParcelada);

	Optional<CompraParcelada> buscarPorId(Long id);

	Optional<CompraParcelada> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<CompraParcelada> listarPorUsuario(Long usuarioId);

	Pagina<CompraParcelada> listarPorUsuario(Long usuarioId, Paginacao paginacao);
}
