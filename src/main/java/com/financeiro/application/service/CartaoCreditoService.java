package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.CartaoCreditoUseCase;
import com.financeiro.application.ports.out.CartaoCreditoRepositoryPort;
import com.financeiro.application.ports.out.CategoriaRepositoryPort;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.application.ports.out.FaturaRepositoryPort;
import com.financeiro.application.ports.out.TransacaoRepositoryPort;
import com.financeiro.application.ports.out.ObterDataAtualPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.CartaoCredito;
import com.financeiro.domain.model.Conta;
import com.financeiro.domain.model.Fatura;
import com.financeiro.domain.model.StatusFatura;
import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.model.Transacao;
import com.financeiro.domain.model.TransacaoHistorico;
import com.financeiro.domain.model.TransacaoItem;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CartaoCreditoService implements CartaoCreditoUseCase {
    private final CartaoCreditoRepositoryPort cartoes;
    private final FaturaRepositoryPort faturas;
    private final ContaRepositoryPort contas;
    private final CategoriaRepositoryPort categorias;
    private final TransacaoRepositoryPort transacoes;
    private final ObterDataAtualPort dataAtual;

    public CartaoCreditoService(CartaoCreditoRepositoryPort cartoes, FaturaRepositoryPort faturas, ContaRepositoryPort contas, CategoriaRepositoryPort categorias, TransacaoRepositoryPort transacoes, ObterDataAtualPort dataAtual) {
        this.cartoes = cartoes; this.faturas = faturas; this.contas = contas; this.categorias = categorias; this.transacoes = transacoes; this.dataAtual = dataAtual;
    }

    public CartaoCredito criarCartao(Long usuarioId, CriarCartaoCommand command) { validarDia(command.diaFechamento()); validarDia(command.diaVencimento()); return cartoes.salvar(CartaoCredito.novo(usuarioId, command.nome(), ValorMonetario.of(command.limite()), command.diaFechamento(), command.diaVencimento())); }
    public List<CartaoCredito> listarCartoes(Long usuarioId) { return cartoes.listarPorUsuario(usuarioId); }
    public Pagina<CartaoCredito> listarCartoes(Long usuarioId, Paginacao paginacao) { return cartoes.listarPorUsuario(usuarioId, paginacao); }
    public CartaoCredito buscarCartao(Long usuarioId, Long cartaoId) { return require(cartoes.buscarPorIdEUsuario(cartaoId, usuarioId)); }
    public CartaoCredito atualizarCartao(Long usuarioId, Long cartaoId, CriarCartaoCommand command) { validarDia(command.diaFechamento()); validarDia(command.diaVencimento()); CartaoCredito atual = buscarCartao(usuarioId, cartaoId); return cartoes.salvar(CartaoCredito.reconstituir(atual.getId(), usuarioId, command.nome(), ValorMonetario.of(command.limite()), command.diaFechamento(), command.diaVencimento(), atual.isAtivo())); }
    public CartaoCredito inativarCartao(Long usuarioId, Long cartaoId) { CartaoCredito atual = buscarCartao(usuarioId, cartaoId); return cartoes.salvar(CartaoCredito.reconstituir(atual.getId(), usuarioId, atual.getNome(), atual.getLimite(), atual.getDiaFechamento(), atual.getDiaVencimento(), false)); }
    public Fatura criarFatura(Long usuarioId, CriarFaturaCommand command) { if (!buscarCartao(usuarioId, command.cartaoId()).isAtivo()) throw new DomainException("error.cartao.inativo"); ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaPagamentoId(), usuarioId))); AnoMes mes = AnoMes.parse(command.anoMes()); if (faturas.buscarPorCartaoEMes(command.cartaoId(), mes).isPresent()) throw new DomainException("error.fatura.existe"); return faturas.salvar(Fatura.nova(command.cartaoId(), mes, command.dataFechamento(), command.dataVencimento(), command.contaPagamentoId())); }
    public List<Fatura> listarFaturas(Long usuarioId, Long cartaoId) { buscarCartao(usuarioId, cartaoId); return faturas.listarPorCartao(cartaoId); }
    public Pagina<Fatura> listarFaturas(Long usuarioId, Long cartaoId, Paginacao paginacao) { buscarCartao(usuarioId, cartaoId); return faturas.listarPorCartao(cartaoId, paginacao); }
    public Fatura buscarFatura(Long usuarioId, Long faturaId) { Fatura fatura = require(faturas.buscarPorId(faturaId)); buscarCartao(usuarioId, fatura.getCartaoId()); return fatura; }
    public Transacao lancarGasto(Long usuarioId, LancarGastoCommand command) { Fatura fatura = buscarFaturaParaAtualizacao(usuarioId, command.faturaId()); CartaoCredito cartao = buscarCartao(usuarioId, fatura.getCartaoId()); if (!cartao.isAtivo()) throw new DomainException("error.cartao.inativo"); if (fatura.getStatus() != StatusFatura.ABERTA) throw new DomainException("error.fatura.fechada"); if (valorFatura(fatura.getId()).somar(ValorMonetario.of(command.valor())).valor().compareTo(cartao.getLimite().valor()) > 0) throw new DomainException("error.cartao.limite.excedido"); ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaId(), usuarioId))); if (command.categoriaId() != null) require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId)); List<TransacaoItem> itens = command.itens() == null ? List.of() : command.itens().stream().map(item -> { if (item.categoriaId() != null) require(categorias.buscarPorIdEUsuario(item.categoriaId(), usuarioId)); return TransacaoItem.novo(item.descricao(), ValorMonetario.of(item.valor()), item.categoriaId()); }).toList(); Transacao transacao = transacoes.salvar(Transacao.gastoCartao(usuarioId, ValorMonetario.of(command.valor()), command.data(), command.descricao(), command.contaId(), command.categoriaId(), command.faturaId(), itens)); transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "GASTO_CARTAO", null, transacao.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora())); return transacao; }
    public Fatura fechar(Long usuarioId, Long faturaId) { Fatura fatura = buscarFatura(usuarioId, faturaId); fatura.fechar(); return faturas.salvar(fatura); }
    public Fatura pagar(Long usuarioId, Long faturaId, LocalDate dataPagamento) { Fatura fatura = buscarFatura(usuarioId, faturaId); ValorMonetario valor = valorFatura(faturaId); Conta conta = ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(fatura.getContaPagamentoId(), usuarioId))); Transacao pagamento = transacoes.salvar(Transacao.nova(usuarioId, TipoTransacao.SAIDA, valor, dataPagamento, "Pagamento fatura " + fatura.getMesReferencia().formatado(), conta.getId(), null, null, List.of())); contas.salvar(Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(), conta.getBancoId(), conta.getSaldo().subtrair(valor), conta.isAtivo(), conta.getVersion())); transacoes.salvarHistorico(TransacaoHistorico.registrar(pagamento.getId(), "PAGAMENTO_FATURA", null, valor.valor().toPlainString(), usuarioId, dataAtual.obterDataHora())); fatura.pagar(); return faturas.salvar(fatura); }
    private Fatura buscarFaturaParaAtualizacao(Long usuarioId, Long faturaId) { Fatura fatura = require(faturas.buscarPorIdParaAtualizacao(faturaId)); buscarCartao(usuarioId, fatura.getCartaoId()); return fatura; }
    private ValorMonetario valorFatura(Long faturaId) { return ValorMonetario.of(transacoes.listarPorFatura(faturaId).stream().filter(transacao -> !transacao.isEstornada()).map(transacao -> transacao.getValor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add)); }
    private void validarDia(int dia) { if (dia < 1 || dia > 31) throw new DomainException("error.cartao.dia.invalido"); }
    private <T> T require(Optional<T> valor) { return valor.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")); }
}
