package com.finisus.application.ports.out;

import com.finisus.domain.model.Banco;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;

public interface BancoRepositoryPort {

	Banco salvar(Banco banco);

	Optional<Banco> buscarPorId(Long id);

	List<Banco> listarSistema();

	List<Banco> listarPorUsuario(Long usuarioId);

	List<Banco> listarDisponiveisParaUsuario(Long usuarioId);

	Pagina<Banco> listarDisponiveisParaUsuario(Long usuarioId, Paginacao paginacao);
}
