package com.finisus.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.CartaoCreditoUseCase;
import com.finisus.application.ports.in.FaturaUseCase;
import com.finisus.application.ports.in.EstornarTransacaoVinculadaUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.AplicacaoCreditoFaturaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.FaturaRepositoryPort;
import com.finisus.application.ports.out.ItemRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.PagamentoFaturaRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.CartaoCredito;
import com.finisus.domain.model.AplicacaoCreditoFatura;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.model.Item;
import com.finisus.domain.model.PagamentoFatura;
import com.finisus.domain.model.StatusFatura;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.model.TransacaoItem;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.vo.AnoMes;
import com.finisus.domain.vo.ValorMonetario;

public class FaturaService implements FaturaUseCase {
	private final FaturaRepositoryPort faturas;
	private final CartaoCreditoUseCase cartoes;
	private final ContaRepositoryPort contas;
	private final CategoriaRepositoryPort categorias;
	private final ItemRepositoryPort itensCatalogo;
	private final TransacaoRepositoryPort transacoes;
	private final ObterDataAtualPort dataAtual;
	private final EstornarTransacaoVinculadaUseCase estornos;
	private final PagamentoFaturaRepositoryPort pagamentos;
	private final AplicacaoCreditoFaturaRepositoryPort aplicacoesCredito;

	public FaturaService(FaturaRepositoryPort faturas, CartaoCreditoUseCase cartoes, ContaRepositoryPort contas,
			CategoriaRepositoryPort categorias, ItemRepositoryPort itensCatalogo, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual, EstornarTransacaoVinculadaUseCase estornos,
			PagamentoFaturaRepositoryPort pagamentos, AplicacaoCreditoFaturaRepositoryPort aplicacoesCredito) {
		this.faturas = faturas;
		this.cartoes = cartoes;
		this.contas = contas;
		this.categorias = categorias;
		this.itensCatalogo = itensCatalogo;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
		this.estornos = estornos;
		this.pagamentos = pagamentos;
		this.aplicacoesCredito = aplicacoesCredito;
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
		BigDecimal saldoDocumento = saldoDocumento(transacoesDaFatura);
		BigDecimal valorTotal = saldoDocumento.max(BigDecimal.ZERO);
		BigDecimal creditoDocumento = saldoDocumento.negate().max(BigDecimal.ZERO);
		BigDecimal valorPago = valorPago(faturaId, usuarioId);
		BigDecimal creditoAplicado = creditoAplicadoNaFatura(faturaId);
		BigDecimal liquidado = valorPago.add(creditoAplicado);
		return new Detalhe(fatura, transacoesDaFatura, valorTotal, valorPago, creditoAplicado,
				valorTotal.subtract(liquidado).max(BigDecimal.ZERO),
				liquidado.subtract(valorTotal).max(BigDecimal.ZERO).add(creditoDocumento));
	}

	@Override
	@Transactional(readOnly = true)
	public java.util.Map<Long, BigDecimal> buscarValoresEmAberto(Long usuarioId, List<Long> faturasIds) {
		if (faturasIds.isEmpty()) return java.util.Map.of();
		var solicitadas = faturasIds.stream().collect(java.util.stream.Collectors.toSet());
		var autorizadas = faturas.listarEmAbertoPorUsuario(usuarioId).stream()
				.map(Fatura::getId).filter(solicitadas::contains).collect(java.util.stream.Collectors.toSet());
		if (!autorizadas.equals(solicitadas)) throw new DomainException("error.recurso.nao.encontrado");
		var transacoesPorFatura = transacoes.listarPorFaturas(faturasIds);
		var pagamentosPorFatura = pagamentos.listarPorFaturasEUsuario(faturasIds, usuarioId);
		var creditosPorFatura = aplicacoesCredito.somarPorFaturasDestino(faturasIds);
		return faturasIds.stream().distinct().collect(java.util.stream.Collectors.toMap(java.util.function.Function.identity(), id -> {
			BigDecimal total = saldoDocumento(transacoesPorFatura.getOrDefault(id, List.of())).max(BigDecimal.ZERO);
			BigDecimal pago = pagamentosPorFatura.getOrDefault(id, List.of()).stream().filter(PagamentoFatura::ativo)
					.map(pagamento -> pagamento.valor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add);
			return total.subtract(pago).subtract(creditosPorFatura.getOrDefault(id, BigDecimal.ZERO)).max(BigDecimal.ZERO);
		}));
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
			CategoriaAtivaValidator.exigirAtiva(
					require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId)));
		}
		List<TransacaoItem> itens = command.itens() == null ? List.of() : command.itens().stream().map(item -> {
			Item catalogo = item.itemId() == null ? null
					: require(itensCatalogo.buscarPorIdEUsuario(item.itemId(), usuarioId));
			if (catalogo != null && !catalogo.isAtivo())
				throw new DomainException("error.item.inativo");
			String descricao = item.descricao() == null || item.descricao().isBlank()
					? catalogo == null ? null : catalogo.getNome()
					: item.descricao();
			Long categoriaId = item.categoriaId() != null ? item.categoriaId()
					: catalogo == null ? null : catalogo.getCategoriaPadraoId();
			if (item.categoriaId() != null)
				CategoriaAtivaValidator.exigirAtiva(
						require(categorias.buscarPorIdEUsuario(item.categoriaId(), usuarioId)));
			else if (categoriaId != null)
				CategoriaAtivaValidator.exigirAtiva(
						require(categorias.buscarPorIdEUsuario(categoriaId, usuarioId)));
			return TransacaoItem.novo(catalogo == null ? null : catalogo.getId(), descricao, item.quantidade(),
					ValorMonetario.of(item.valor()), categoriaId);
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
	public Transacao lancarCredito(Long usuarioId, LancarGastoCommand command) {
		Fatura fatura = buscarParaAtualizacao(usuarioId, command.faturaId());
		CartaoCredito cartao = cartoes.buscarCartao(usuarioId, fatura.getCartaoId());
		if (!cartao.isAtivo()) throw new DomainException("error.cartao.inativo");
		if (fatura.getStatus() != StatusFatura.ABERTA) throw new DomainException("error.fatura.fechada");
		ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)));
		if (command.categoriaId() != null)
			CategoriaAtivaValidator.exigirAtiva(require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId)));
		List<TransacaoItem> itens = command.itens() == null ? List.of() : command.itens().stream().map(item -> {
			Item catalogo = item.itemId() == null ? null : require(itensCatalogo.buscarPorIdEUsuario(item.itemId(), usuarioId));
			if (catalogo != null && !catalogo.isAtivo()) throw new DomainException("error.item.inativo");
			String descricao = item.descricao() == null || item.descricao().isBlank()
					? catalogo == null ? null : catalogo.getNome() : item.descricao();
			Long categoriaId = item.categoriaId() != null ? item.categoriaId()
					: catalogo == null ? null : catalogo.getCategoriaPadraoId();
			if (categoriaId != null)
				CategoriaAtivaValidator.exigirAtiva(require(categorias.buscarPorIdEUsuario(categoriaId, usuarioId)));
			return TransacaoItem.novo(catalogo == null ? null : catalogo.getId(), descricao, item.quantidade(),
					ValorMonetario.of(item.valor()), categoriaId);
		}).toList();
		Transacao transacao = transacoes.salvar(Transacao.creditoFatura(usuarioId,
				ValorMonetario.of(command.valor()), command.data(), command.descricao(), command.contaId(),
				command.categoriaId(), command.faturaId(), itens));
		transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "CREDITO_FATURA", null,
				transacao.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
		return transacao;
	}

	@Override
	@Transactional
	public Fatura fechar(Long usuarioId, Long faturaId) {
		Fatura fatura = buscarParaAtualizacao(usuarioId, faturaId);
		fatura.fechar();
		aplicarCreditosDisponiveis(usuarioId, fatura);
		return faturas.salvar(fatura);
	}

	@Override
	@Transactional
	public ResultadoProcessamentoCiclo processarCiclos(Long usuarioId, LocalDate dataReferencia) {
		if (dataReferencia == null) throw new DomainException("error.fatura.ciclo.data.invalida");
		List<Fatura> fechadas = new java.util.ArrayList<>();
		List<Fatura> criadas = new java.util.ArrayList<>();
		List<Fatura> pendentes;
		do {
			pendentes = faturas.listarAbertasParaFechamento(usuarioId, dataReferencia);
			for (Fatura fatura : pendentes) {
				CartaoCredito cartao = cartoes.buscarCartao(usuarioId, fatura.getCartaoId());
				fatura.fechar();
				aplicarCreditosDisponiveis(usuarioId, fatura);
				fechadas.add(faturas.salvar(fatura));
				AnoMes proximoMes = fatura.getMesReferencia().proximo();
				if (cartao.isAtivo() && faturas.buscarPorCartaoEMes(cartao.getId(), proximoMes).isEmpty()) {
					YearMonth competencia = YearMonth.parse(proximoMes.formatado());
					LocalDate fechamento = dataNoMes(competencia, cartao.getDiaFechamento());
					YearMonth mesVencimento = cartao.getDiaVencimento() > cartao.getDiaFechamento()
							? competencia : competencia.plusMonths(1);
					LocalDate vencimento = dataNoMes(mesVencimento, cartao.getDiaVencimento());
					criadas.add(faturas.salvar(Fatura.nova(cartao.getId(), proximoMes, fechamento, vencimento,
							fatura.getContaPagamentoId())));
				}
			}
		} while (!pendentes.isEmpty());
		return new ResultadoProcessamentoCiclo(List.copyOf(fechadas), List.copyOf(criadas));
	}

	@Override
	@Transactional
	public Fatura pagar(Long usuarioId, Long faturaId, String chaveIdempotencia, PagamentoCommand command) {
		validarPagamento(chaveIdempotencia, command);
		String chave = chaveIdempotencia.trim();
		String hash = hashPagamento(faturaId, command);
		var existente = pagamentos.buscarPorUsuarioEChave(usuarioId, chave);
		if (existente.isPresent()) return validarRepeticao(usuarioId, faturaId, existente.get(), hash);
		Fatura fatura = buscarParaAtualizacao(usuarioId, faturaId);
		existente = pagamentos.buscarPorUsuarioEChave(usuarioId, chave);
		if (existente.isPresent()) return validarRepeticao(usuarioId, faturaId, existente.get(), hash);
		if (fatura.getStatus() != StatusFatura.FECHADA) throw new DomainException("error.fatura.transicao.invalida");
		BigDecimal saldoEmAberto = valorFatura(faturaId).valor().subtract(valorPago(faturaId, usuarioId))
				.subtract(creditoAplicadoNaFatura(faturaId));
		if (saldoEmAberto.compareTo(BigDecimal.ZERO) <= 0) throw new DomainException("error.fatura.pagamento.invalido");
		ValorMonetario valor = ValorMonetario.of(command.valor() == null ? saldoEmAberto : command.valor());
		ValorMonetario credito = ValorMonetario.of(valor.valor().subtract(saldoEmAberto).max(BigDecimal.ZERO));
		Long contaId = command.contaId() == null ? fatura.getContaPagamentoId() : command.contaId();
		Conta conta = ContaAtivaValidator
				.exigirAtiva(require(contas.buscarPorIdEUsuario(contaId, usuarioId)));
		Transacao pagamento = transacoes.salvar(Transacao.pagamentoFatura(usuarioId, valor, command.dataPagamento(),
				"Pagamento fatura " + fatura.getMesReferencia().formatado(), conta.getId(), fatura.getId()));
		conta.debitar(valor);
		contas.salvar(conta);
		transacoes.salvarHistorico(TransacaoHistorico.registrar(pagamento.getId(), "PAGAMENTO_FATURA", null,
				valor.valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
		pagamentos.salvar(PagamentoFatura.novo(faturaId, usuarioId, pagamento.getId(), conta.getId(), valor, credito,
				command.dataPagamento(), chave, hash));
		if (valor.valor().compareTo(saldoEmAberto) >= 0) {
			fatura.pagar();
			return faturas.salvar(fatura);
		}
		return fatura;
	}

	@Override
	@Transactional
	public Fatura estornarPagamento(Long usuarioId, Long faturaId) {
		Fatura fatura = buscarParaAtualizacao(usuarioId, faturaId);
		List<PagamentoFatura> ativos = pagamentos.listarPorFaturaEUsuario(faturaId, usuarioId).stream()
				.filter(PagamentoFatura::ativo).toList();
		PagamentoFatura registro = ativos.stream().reduce((anterior, atual) -> atual)
				.orElseThrow(() -> new DomainException("error.fatura.pagamento.nao.encontrado"));
		if (aplicacoesCredito.existePorPagamentoOrigem(registro.id()))
			throw new DomainException("error.fatura.credito.ja.aplicado");
		pagamentos.salvar(registro.estornar(dataAtual.obterDataHora()));
		estornos.estornarVinculada(usuarioId, registro.transacaoId());
		BigDecimal restante = ativos.stream().filter(p -> !p.id().equals(registro.id()))
				.map(p -> p.valor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal creditoAplicado = creditoAplicadoNaFatura(faturaId);
		if (fatura.getStatus() == StatusFatura.PAGA
				&& restante.add(creditoAplicado).compareTo(valorFatura(faturaId).valor()) < 0)
			fatura.estornarPagamento();
		return faturas.salvar(fatura);
	}

	private BigDecimal valorPago(Long faturaId, Long usuarioId) {
		return pagamentos.listarPorFaturaEUsuario(faturaId, usuarioId).stream().filter(PagamentoFatura::ativo)
				.map(p -> p.valor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal creditoAplicadoNaFatura(Long faturaId) {
		BigDecimal valor = aplicacoesCredito.somarPorFaturaDestino(faturaId);
		return valor == null ? BigDecimal.ZERO : valor;
	}

	private BigDecimal creditoConsumido(Long pagamentoId) {
		BigDecimal valor = aplicacoesCredito.somarPorPagamentoOrigem(pagamentoId);
		return valor == null ? BigDecimal.ZERO : valor;
	}

	private void aplicarCreditosDisponiveis(Long usuarioId, Fatura destino) {
		BigDecimal restante = valorFatura(destino.getId()).valor()
				.subtract(creditoAplicadoNaFatura(destino.getId())).max(BigDecimal.ZERO);
		if (restante.signum() == 0) return;
		List<Fatura> anteriores = faturas.listarPorCartao(destino.getCartaoId()).stream()
				.filter(f -> f.getMesReferencia().formatado().compareTo(destino.getMesReferencia().formatado()) < 0)
				.sorted(java.util.Comparator.comparing(f -> f.getMesReferencia().formatado())).toList();
		for (Fatura anterior : anteriores) {
			for (PagamentoFatura pagamento : pagamentos.listarPorFaturaEUsuario(anterior.getId(), usuarioId)) {
				if (!pagamento.ativo() || pagamento.credito().isZero()) continue;
				BigDecimal disponivel = pagamento.credito().valor()
						.subtract(creditoConsumido(pagamento.id())).max(BigDecimal.ZERO);
				BigDecimal valorAplicado = disponivel.min(restante);
				if (valorAplicado.signum() > 0) {
					aplicacoesCredito.salvar(AplicacaoCreditoFatura.nova(pagamento.id(), destino.getId(),
							ValorMonetario.of(valorAplicado), dataAtual.obterDataHora()));
					restante = restante.subtract(valorAplicado);
				}
				if (restante.signum() == 0) {
					destino.pagar();
					return;
				}
			}
		}
	}

	private void validarPagamento(String chave, PagamentoCommand command) {
		if (chave == null || chave.isBlank() || chave.length() > 100 || command == null
				|| command.dataPagamento() == null || command.valor() != null
						&& command.valor().compareTo(BigDecimal.ZERO) <= 0)
			throw new DomainException("error.fatura.pagamento.invalido");
	}

	private Fatura validarRepeticao(Long usuarioId, Long faturaId, PagamentoFatura pagamento, String hash) {
		if (!pagamento.faturaId().equals(faturaId) || !pagamento.hashRequisicao().equals(hash))
			throw new DomainException("error.fatura.pagamento.idempotencia.conflitante");
		return buscar(usuarioId, faturaId);
	}

	private String hashPagamento(Long faturaId, PagamentoCommand command) {
		String valor = command.valor() == null ? "TOTAL" : command.valor().stripTrailingZeros().toPlainString();
		String payload = faturaId + "|" + valor + "|" + command.dataPagamento() + "|" + command.contaId();
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(payload.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 indisponível", e);
		}
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
		return ValorMonetario.of(saldoDocumento(transacoes.listarPorFatura(faturaId)).max(BigDecimal.ZERO));
	}

	private BigDecimal saldoDocumento(List<Transacao> transacoesDaFatura) {
		return transacoesDaFatura.stream().filter(transacao -> !transacao.isEstornada())
				.map(transacao -> transacao.getTipo() == TipoTransacao.ENTRADA
						? transacao.getValor().valor().negate() : transacao.getValor().valor())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private <T> T require(Optional<T> valor) {
		return valor.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}

	private LocalDate dataNoMes(YearMonth mes, int dia) {
		return mes.atDay(Math.min(dia, mes.lengthOfMonth()));
	}
}
