package com.financeiro.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.CartaoCreditoUseCase;
import com.financeiro.application.ports.in.FaturaUseCase;
import com.financeiro.application.ports.in.TransacaoUseCase;
import com.financeiro.application.ports.out.CategoriaRepositoryPort;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.application.ports.out.FaturaRepositoryPort;
import com.financeiro.application.ports.out.ItemRepositoryPort;
import com.financeiro.application.ports.out.ObterDataAtualPort;
import com.financeiro.application.ports.out.TransacaoRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.CartaoCredito;
import com.financeiro.domain.model.Conta;
import com.financeiro.domain.model.Fatura;
import com.financeiro.domain.model.Item;
import com.financeiro.domain.model.StatusFatura;
import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.model.Transacao;
import com.financeiro.domain.model.TransacaoHistorico;
import com.financeiro.domain.model.TransacaoItem;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;

public class FaturaService implements FaturaUseCase {
	private final FaturaRepositoryPort faturas;
	private final CartaoCreditoUseCase cartoes;
	private final ContaRepositoryPort contas;
	private final CategoriaRepositoryPort categorias;
	private final ItemRepositoryPort itensCatalogo;
	private final TransacaoRepositoryPort transacoes;
	private final ObterDataAtualPort dataAtual;

	public FaturaService(FaturaRepositoryPort faturas, CartaoCreditoUseCase cartoes, ContaRepositoryPort contas,
			CategoriaRepositoryPort categorias, ItemRepositoryPort itensCatalogo, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual) {
		this.faturas = faturas;
		this.cartoes = cartoes;
		this.contas = contas;
		this.categorias = categorias;
		this.itensCatalogo = itensCatalogo;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
	}

	@Override
	@Transactional
	public Fatura criar(Long usuarioId, CriarCommand command) {
		CartaoCredito cartao = cartoes.buscarCartao(usuarioId, command.cartaoId());
		if (!cartao.isAtivo()) {
			throw new DomainException("error.cartao.inativo");
		}
		ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaPagamentoId(), usuarioId)));
		AnoMes mes = AnoMes.parse(command.anoMes());
		if (faturas.buscarPorCartaoEMes(command.cartaoId(), mes).isPresent()) {
			throw new DomainException("error.fatura.existe");
		}
		return faturas.salvar(Fatura.nova(command.cartaoId(), mes, command.dataFechamento(), command.dataVencimento(),
				command.contaPagamentoId()));
	}

	@Override
	@Transactional(readOnly = true)
	public Fatura buscar(Long usuarioId, Long faturaId) {
		Fatura fatura = require(faturas.buscarPorId(faturaId));
		cartoes.buscarCartao(usuarioId, fatura.getCartaoId());
		return fatura;
	}

	@Override
	@Transactional(readOnly = true)
	public Detalhe buscarDetalhe(Long usuarioId, Long faturaId) {
		Fatura fatura = buscar(usuarioId, faturaId);
		List<Transacao> transacoesDaFatura = transacoes.listarPorFatura(faturaId);
		BigDecimal valorTotal = transacoesDaFatura.stream().filter(transacao -> !transacao.isEstornada())
				.map(transacao -> transacao.getValor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new Detalhe(fatura, transacoesDaFatura, valorTotal);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Fatura> listar(Long usuarioId, Long cartaoId) {
		cartoes.buscarCartao(usuarioId, cartaoId);
		return faturas.listarPorCartao(cartaoId);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<Fatura> listar(Long usuarioId, Long cartaoId, Paginacao paginacao) {
		cartoes.buscarCartao(usuarioId, cartaoId);
		return faturas.listarPorCartao(cartaoId, paginacao);
	}

	@Override
	@Transactional
	public Transacao lancarGasto(Long usuarioId, LancarGastoCommand command) {
		Fatura fatura = buscarParaAtualizacao(usuarioId, command.faturaId());
		CartaoCredito cartao = cartoes.buscarCartao(usuarioId, fatura.getCartaoId());
		if (!cartao.isAtivo()) {
			throw new DomainException("error.cartao.inativo");
		}
		if (fatura.getStatus() != StatusFatura.ABERTA) {
			throw new DomainException("error.fatura.fechada");
		}
		if (valorFatura(fatura.getId()).somar(ValorMonetario.of(command.valor())).valor()
				.compareTo(cartao.getLimite().valor()) > 0) {
			throw new DomainException("error.cartao.limite.excedido");
		}
		ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)));
		if (command.categoriaId() != null) {
			require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId));
		}
		List<TransacaoItem> itens = command.itens() == null ? List.of() : command.itens().stream().map(item -> {
			Item catalogo = require(itensCatalogo.buscarPorIdEUsuario(item.itemId(), usuarioId));
			if (!catalogo.isAtivo())
				throw new DomainException("error.item.inativo");
			return TransacaoItem.novo(catalogo.getId(), catalogo.getNome(), ValorMonetario.of(item.valor()),
					catalogo.getCategoriaPadraoId());
		}).toList();
		Transacao transacao = transacoes
				.salvar(Transacao.gastoCartao(usuarioId, ValorMonetario.of(command.valor()), command.data(),
						command.descricao(), command.contaId(), command.categoriaId(), command.faturaId(), itens));
		transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "GASTO_CARTAO", null,
				transacao.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
		return transacao;
	}

	@Override
	@Transactional
	public Fatura fechar(Long usuarioId, Long faturaId) {
		Fatura fatura = buscarParaAtualizacao(usuarioId, faturaId);
		fatura.fechar();
		return faturas.salvar(fatura);
	}

	@Override
	@Transactional
	public Fatura pagar(Long usuarioId, Long faturaId, LocalDate dataPagamento) {
		Fatura fatura = buscarParaAtualizacao(usuarioId, faturaId);
		ValorMonetario valor = valorFatura(faturaId);
		Conta conta = ContaAtivaValidator
				.exigirAtiva(require(contas.buscarPorIdEUsuario(fatura.getContaPagamentoId(), usuarioId)));
		Transacao pagamento = transacoes.salvar(Transacao.nova(usuarioId, TipoTransacao.SAIDA, valor, dataPagamento,
				"Pagamento fatura " + fatura.getMesReferencia().formatado(), conta.getId(), null, null, List.of()));
		contas.salvar(Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(),
				conta.getBancoId(), conta.getSaldo().subtrair(valor), conta.isAtivo(), conta.getVersion()));
		transacoes.salvarHistorico(TransacaoHistorico.registrar(pagamento.getId(), "PAGAMENTO_FATURA", null,
				valor.valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
		fatura.pagar();
		return faturas.salvar(fatura);
	}

	@Override
	@Transactional
	public Fatura atualizar(Long usuarioId, Long faturaId, AtualizarCommand command) {
		Fatura fatura = buscarParaAtualizacao(usuarioId, faturaId);
		ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaPagamentoId(), usuarioId)));
		return faturas.salvar(
				fatura.atualizar(command.dataFechamento(), command.dataVencimento(), command.contaPagamentoId()));
	}

	@Override
	@Transactional
	public Fatura cancelar(Long usuarioId, Long faturaId) {
		Fatura fatura = buscarParaAtualizacao(usuarioId, faturaId);
		return faturas.salvar(fatura.cancelar(dataAtual.obterDataHora().toLocalDate()));
	}

	private Fatura buscarParaAtualizacao(Long usuarioId, Long faturaId) {
		Fatura fatura = require(faturas.buscarPorIdParaAtualizacao(faturaId));
		cartoes.buscarCartao(usuarioId, fatura.getCartaoId());
		return fatura;
	}

	private ValorMonetario valorFatura(Long faturaId) {
		return ValorMonetario
				.of(transacoes.listarPorFatura(faturaId).stream().filter(transacao -> !transacao.isEstornada())
						.map(transacao -> transacao.getValor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add));
	}

	private <T> T require(Optional<T> valor) {
		return valor.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}
}
