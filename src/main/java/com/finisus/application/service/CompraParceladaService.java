package com.finisus.application.service;

import com.finisus.application.ports.in.CompraParceladaUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.CompraParceladaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.CompraParcelada;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class CompraParceladaService implements CompraParceladaUseCase {
	private final CompraParceladaRepositoryPort compras;
	private final ContaRepositoryPort contas;
	private final CategoriaRepositoryPort categorias;
	private final TransacaoRepositoryPort transacoes;
	private final ObterDataAtualPort dataAtual;

	public CompraParceladaService(CompraParceladaRepositoryPort compras, ContaRepositoryPort contas,
			CategoriaRepositoryPort categorias, TransacaoRepositoryPort transacoes, ObterDataAtualPort dataAtual) {
		this.compras = compras;
		this.contas = contas;
		this.categorias = categorias;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
	}

	@Transactional
	public CompraParcelada criar(Long usuarioId, CriarCommand command) {
		if (command.numeroParcelas() < 1)
			throw new DomainException("error.parcelamento.quantidade.invalida");
		ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)));
		if (command.categoriaId() != null)
			require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId));
		CompraParcelada compra = compras
				.salvar(CompraParcelada.nova(usuarioId, command.descricao(), ValorMonetario.of(command.valorTotal()),
						command.numeroParcelas(), command.dataCompra(), command.categoriaId(), command.contaId()));
		BigDecimal parcelaBase = compra.getValorTotal().valor().divide(BigDecimal.valueOf(compra.getNumeroParcelas()),
				2, RoundingMode.DOWN);
		BigDecimal acumulado = BigDecimal.ZERO;
		for (int numero = 1; numero <= compra.getNumeroParcelas(); numero++) {
			BigDecimal valor = numero == compra.getNumeroParcelas() ? compra.getValorTotal().valor().subtract(acumulado)
					: parcelaBase;
			acumulado = acumulado.add(valor);
			Transacao transacao = transacoes.salvar(Transacao.geradaPorCompraParcelada(usuarioId,
					ValorMonetario.of(valor), compra.getDataCompra().plusMonths(numero - 1),
					compra.getDescricao() + " (" + numero + "/" + compra.getNumeroParcelas() + ")", compra.getContaId(),
					compra.getCategoriaId(), compra.getId()));
			transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "GERACAO_PARCELA", null,
					valor.toPlainString(), usuarioId, dataAtual.obterDataHora()));
		}
		return compra;
	}

	public List<CompraParcelada> listar(Long usuarioId) {
		return compras.listarPorUsuario(usuarioId);
	}

	public Pagina<CompraParcelada> listar(Long usuarioId, Paginacao paginacao) {
		return compras.listarPorUsuario(usuarioId, paginacao);
	}

	public CompraParcelada buscar(Long usuarioId, Long compraId) {
		return require(compras.buscarPorIdEUsuario(compraId, usuarioId));
	}

	@Transactional
	public CompraParcelada cancelar(Long usuarioId, Long compraId) {
		CompraParcelada compra = buscar(usuarioId, compraId).cancelada(dataAtual.obterDataHora());
		transacoes.listarPorCompraParcelada(compraId).stream()
				.filter(t -> !t.isEstornada() && t.getData().isAfter(dataAtual.obter())).forEach(t -> {
					Transacao estornada = transacoes.salvar(t.estornada(dataAtual.obterDataHora()));
					transacoes.salvarHistorico(TransacaoHistorico.registrar(estornada.getId(), "CANCELAMENTO_COMPRA",
							estornada.getValor().valor().toPlainString(), null, usuarioId, dataAtual.obterDataHora()));
				});
		return compras.salvar(compra);
	}

	private <T> T require(java.util.Optional<T> resource) {
		return resource.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}
}
