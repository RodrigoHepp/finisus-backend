package com.finisus.application.ports.out;

import com.finisus.domain.model.Conta;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface ContaRepositoryPort {

	Conta salvar(Conta conta);

	Optional<Conta> buscarPorId(Long id);

	Optional<Conta> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<Conta> buscarPorIdsEUsuarioParaAtualizacao(List<Long> ids, Long usuarioId);

	List<Conta> listarPorUsuario(Long usuarioId);

	Pagina<Conta> listarPorUsuario(Long usuarioId, Paginacao paginacao);

	boolean existePorIdEUsuario(Long id, Long usuarioId);

	ResumoMovimentosConta calcularMovimentosEficazes(Long contaId, Long usuarioId);

	record ResumoMovimentosConta(BigDecimal saldoCalculado, long quantidade, long quantidadeAjustes) { }
}
