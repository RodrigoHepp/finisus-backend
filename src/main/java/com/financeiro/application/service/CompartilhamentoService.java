package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.CompartilhamentoUseCase;
import com.financeiro.application.ports.out.CompartilhamentoRepositoryPort;
import com.financeiro.application.ports.out.TransacaoRepositoryPort;
import com.financeiro.application.ports.out.UsuarioRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.*;
import com.financeiro.domain.vo.Email;
import com.financeiro.domain.vo.ValorMonetario;

import java.math.BigDecimal;
import java.util.List;

public class CompartilhamentoService implements CompartilhamentoUseCase {
    private final CompartilhamentoRepositoryPort compartilhamentos;
    private final TransacaoRepositoryPort transacoes;
    private final UsuarioRepositoryPort usuarios;
    public CompartilhamentoService(CompartilhamentoRepositoryPort compartilhamentos, TransacaoRepositoryPort transacoes, UsuarioRepositoryPort usuarios) { this.compartilhamentos = compartilhamentos; this.transacoes = transacoes; this.usuarios = usuarios; }

    public ConfiguracaoCompartilhamento atualizarOptIn(Long usuarioId, boolean aceita) { ConfiguracaoCompartilhamento configuracao = compartilhamentos.buscarConfiguracao(usuarioId).orElseGet(() -> ConfiguracaoCompartilhamento.padrao(usuarioId)); configuracao.atualizarOptIn(aceita); return compartilhamentos.salvarConfiguracao(configuracao); }
    public ConfiguracaoCompartilhamento consultarOptIn(Long usuarioId) { return compartilhamentos.buscarConfiguracao(usuarioId).orElseGet(() -> ConfiguracaoCompartilhamento.padrao(usuarioId)); }
    public Resultado criarDespesa(Long usuarioId, CriarCommand command) { Transacao transacao = require(transacoes.buscarPorIdEUsuario(command.transacaoId(), usuarioId)); if (transacao.getTipo() != TipoTransacao.SAIDA) throw new DomainException("error.compartilhamento.apenas.saida"); DespesaCompartilhada despesa = compartilhamentos.salvarDespesa(DespesaCompartilhada.nova(transacao.getId(), usuarioId, command.tipoRateio())); transacoes.salvar(transacao.associadaADespesaCompartilhada(despesa.getId())); List<RateioDespesa> rateios = command.participantes().stream().map(participante -> criarParticipante(despesa.getId(), command.tipoRateio(), participante)).toList(); validarTotal(command.tipoRateio(), rateios, transacao.getValor().valor()); return new Resultado(despesa, compartilhamentos.salvarRateios(despesa.getId(), rateios)); }
    private RateioDespesa criarParticipante(Long despesaId, TipoRateio tipoRateio, ParticipanteCommand participante) { if (participante.usuarioId() != null) { usuarios.buscarPorId(participante.usuarioId()).orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")); ConfiguracaoCompartilhamento configuracao = compartilhamentos.buscarConfiguracao(participante.usuarioId()).orElseThrow(() -> new DomainException("error.compartilhamento.optin.obrigatorio")); if (!configuracao.isAceitaCompartilhamento()) throw new DomainException("error.compartilhamento.optin.obrigatorio"); return RateioDespesa.interno(despesaId, participante.usuarioId(), valor(tipoRateio, participante.valorFixo()), percentual(tipoRateio, participante.percentual())); } new Email(participante.emailExterno()); if (participante.nomeExterno() == null || participante.nomeExterno().isBlank()) throw new DomainException("error.compartilhamento.externo.invalido"); return RateioDespesa.externo(despesaId, participante.nomeExterno(), participante.emailExterno(), valor(tipoRateio, participante.valorFixo()), percentual(tipoRateio, participante.percentual())); }
    private ValorMonetario valor(TipoRateio tipo, BigDecimal valor) { return tipo == TipoRateio.VALOR_FIXO ? ValorMonetario.of(valor) : null; }
    private BigDecimal percentual(TipoRateio tipo, BigDecimal percentual) { if (tipo == TipoRateio.PERCENTUAL && (percentual == null || percentual.signum() <= 0)) throw new DomainException("error.compartilhamento.percentual.invalido"); return tipo == TipoRateio.PERCENTUAL ? percentual : null; }
    private void validarTotal(TipoRateio tipo, List<RateioDespesa> rateios, BigDecimal total) { BigDecimal soma = tipo == TipoRateio.VALOR_FIXO ? rateios.stream().map(rateio -> rateio.getValorFixo().valor()).reduce(BigDecimal.ZERO, BigDecimal::add) : rateios.stream().map(RateioDespesa::getPercentual).reduce(BigDecimal.ZERO, BigDecimal::add); if ((tipo == TipoRateio.VALOR_FIXO && soma.compareTo(total) > 0) || (tipo == TipoRateio.PERCENTUAL && soma.compareTo(BigDecimal.valueOf(100)) > 0)) throw new DomainException("error.compartilhamento.total.invalido"); }

    public List<DespesaCompartilhada> listarDespesas(Long usuarioId) { return compartilhamentos.listarDespesasPorCriador(usuarioId); }
    public Pagina<DespesaCompartilhada> listarDespesas(Long usuarioId, Paginacao paginacao) { return compartilhamentos.listarDespesasPorCriador(usuarioId, paginacao); }
    public List<RateioDespesa> listarRateios(Long usuarioId, Long despesaId) { require(compartilhamentos.buscarDespesaPorIdECriador(despesaId, usuarioId)); return compartilhamentos.listarRateios(despesaId); }
    public Pagina<RateioDespesa> listarRateios(Long usuarioId, Long despesaId, Paginacao paginacao) { require(compartilhamentos.buscarDespesaPorIdECriador(despesaId, usuarioId)); return compartilhamentos.listarRateios(despesaId, paginacao); }
    public RateioDespesa responderRateio(Long usuarioId, Long rateioId, boolean aceita) { RateioDespesa rateio = require(compartilhamentos.buscarRateio(rateioId)); if (rateio.getTipoParticipante() != TipoParticipante.INTERNO || !usuarioId.equals(rateio.getUsuarioId())) throw new DomainException("error.recurso.nao.encontrado"); if (aceita) rateio.aceitar(); else rateio.recusar(); return compartilhamentos.salvarRateio(rateio); }
    public RateioDespesa marcarRateioPago(Long usuarioId, Long rateioId) { RateioDespesa rateio = require(compartilhamentos.buscarRateio(rateioId)); require(compartilhamentos.buscarDespesaPorIdECriador(rateio.getDespesaCompartilhadaId(), usuarioId)); rateio.pagar(); return compartilhamentos.salvarRateio(rateio); }
    private <T> T require(java.util.Optional<T> value) { return value.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")); }
}
