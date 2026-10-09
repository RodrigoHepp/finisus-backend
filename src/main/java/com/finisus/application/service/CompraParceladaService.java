package com.finisus.application.service;

import com.finisus.application.ports.in.CompraParceladaUseCase;
import com.finisus.application.ports.in.CartaoCreditoUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.CompraParceladaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.FaturaRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.CompraParcelada;
import com.finisus.domain.model.CartaoCredito;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.model.StatusFatura;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.vo.ValorMonetario;
import com.finisus.domain.vo.AnoMes;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.transaction.annotation.Transactional;

public class CompraParceladaService implements CompraParceladaUseCase {
	private final CompraParceladaRepositoryPort compras;
	private final ContaRepositoryPort contas;
	private final CategoriaRepositoryPort categorias;
	private final TransacaoRepositoryPort transacoes;
	private final FaturaRepositoryPort faturas;
	private final CartaoCreditoUseCase cartoes;
	private final ObterDataAtualPort dataAtual;

	public CompraParceladaService(CompraParceladaRepositoryPort compras, ContaRepositoryPort contas,
			CategoriaRepositoryPort categorias, TransacaoRepositoryPort transacoes, FaturaRepositoryPort faturas,
			CartaoCreditoUseCase cartoes, ObterDataAtualPort dataAtual) {
		this.compras = compras;
		this.contas = contas;
		this.categorias = categorias;
		this.transacoes = transacoes;
		this.faturas = faturas;
		this.cartoes = cartoes;
		this.dataAtual = dataAtual;
	}

	@Transactional
	public CompraParcelada criar(Long usuarioId, CriarCommand command) {
		if (command.numeroParcelas() < 1)
			throw new DomainException("error.parcelamento.quantidade.invalida");
		ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)));
		if (command.categoriaId() != null)
			CategoriaAtivaValidator.exigirAtiva(
					require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId)));
		CartaoCredito cartao = command.cartaoId() == null ? null : cartoes.buscarCartao(usuarioId, command.cartaoId());
		if (cartao != null && !cartao.isAtivo())
			throw new DomainException("error.cartao.inativo");
		CompraParcelada compra = compras
				.salvar(CompraParcelada.nova(usuarioId, command.descricao(), ValorMonetario.of(command.valorTotal()),
						command.numeroParcelas(), command.dataCompra(), command.categoriaId(), command.contaId(),
						command.cartaoId()));
		BigDecimal parcelaBase = compra.getValorTotal().valor().divide(BigDecimal.valueOf(compra.getNumeroParcelas()),
				2, RoundingMode.DOWN);
		BigDecimal acumulado = BigDecimal.ZERO;
		for (int numero = 1; numero <= compra.getNumeroParcelas(); numero++) {
			BigDecimal valor = numero == compra.getNumeroParcelas() ? compra.getValorTotal().valor().subtract(acumulado)
					: parcelaBase;
			acumulado = acumulado.add(valor);
			LocalDate dataParcela = compra.getDataCompra().plusMonths(numero - 1);
			Fatura fatura = cartao == null ? null : obterOuCriarFatura(cartao, compra.getContaId(), dataParcela,
					usuarioId);
			if (fatura != null && valorDaFatura(fatura.getId()).add(valor).compareTo(cartao.getLimite().valor()) > 0)
				throw new DomainException("error.cartao.limite.excedido");
			Transacao transacao = transacoes.salvar(Transacao.geradaPorCompraParcelada(usuarioId,
					ValorMonetario.of(valor), dataParcela,
					compra.getDescricao() + " (" + numero + "/" + compra.getNumeroParcelas() + ")", compra.getContaId(),
					compra.getCategoriaId(), compra.getId(), fatura == null ? null : fatura.getId()));
			transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "GERACAO_PARCELA", null,
					valor.toPlainString(), usuarioId, dataAtual.obterDataHora()));
		}
		return compra;
	}

	private Fatura obterOuCriarFatura(CartaoCredito cartao, Long contaPagamentoId, LocalDate dataCompra,
			Long usuarioId) {
		YearMonth mesCompra = YearMonth.from(dataCompra);
		LocalDate fechamentoDoMes = dataNoMes(mesCompra, cartao.getDiaFechamento());
		YearMonth competencia = dataCompra.isAfter(fechamentoDoMes) ? mesCompra.plusMonths(1) : mesCompra;
		AnoMes anoMes = AnoMes.parse(competencia.toString());
		Fatura fatura = faturas.buscarPorCartaoEMes(cartao.getId(), anoMes).orElseGet(() -> {
			LocalDate fechamento = dataNoMes(competencia, cartao.getDiaFechamento());
			YearMonth mesVencimento = cartao.getDiaVencimento() > cartao.getDiaFechamento()
					? competencia : competencia.plusMonths(1);
			return faturas.salvar(Fatura.nova(cartao.getId(), anoMes, fechamento,
					dataNoMes(mesVencimento, cartao.getDiaVencimento()), contaPagamentoId));
		});
		cartoes.buscarCartao(usuarioId, fatura.getCartaoId());
		if (fatura.getStatus() != StatusFatura.ABERTA)
			throw new DomainException("error.fatura.fechada");
		return fatura;
	}

	private BigDecimal valorDaFatura(Long faturaId) {
		return transacoes.listarPorFatura(faturaId).stream().filter(t -> !t.isEstornada())
				.map(t -> t.getValor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private LocalDate dataNoMes(YearMonth mes, int dia) {
		return mes.atDay(Math.min(dia, mes.lengthOfMonth()));
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
				.filter(t -> !t.isEstornada() && podeCancelarParcela(t)).forEach(t -> {
					Transacao estornada = transacoes.salvar(t.estornada(dataAtual.obterDataHora()));
					transacoes.salvarHistorico(TransacaoHistorico.registrar(estornada.getId(), "CANCELAMENTO_COMPRA",
							estornada.getValor().valor().toPlainString(), null, usuarioId, dataAtual.obterDataHora()));
				});
		return compras.salvar(compra);
	}

	private boolean podeCancelarParcela(Transacao transacao) {
		if (transacao.getFaturaId() == null)
			return transacao.getData().isAfter(dataAtual.obter());
		return faturas.buscarPorId(transacao.getFaturaId()).map(fatura -> fatura.getStatus() == StatusFatura.ABERTA)
				.orElse(false);
	}

	private <T> T require(java.util.Optional<T> resource) {
		return resource.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}
}
