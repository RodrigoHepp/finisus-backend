package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.FinanceiroCoreUseCase;
import com.financeiro.application.ports.in.InvestimentoUseCase;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.application.ports.out.InvestimentoRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Conta;
import com.financeiro.domain.model.Investimento;
import com.financeiro.domain.model.MovimentoInvestimento;
import com.financeiro.domain.model.TipoMovimentoInvestimento;
import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.model.Transacao;
import com.financeiro.domain.vo.ValorMonetario;

import java.util.List;

public class InvestimentoService implements InvestimentoUseCase {
    private final InvestimentoRepositoryPort investimentos;
    private final ContaRepositoryPort contas;
    private final FinanceiroCoreUseCase transacoes;

    public InvestimentoService(InvestimentoRepositoryPort investimentos, ContaRepositoryPort contas, FinanceiroCoreUseCase transacoes) {
        this.investimentos = investimentos;
        this.contas = contas;
        this.transacoes = transacoes;
    }

    public Investimento criar(Long usuarioId, CriarCommand command) {
        conta(command.contaOrigemId(), usuarioId);
        return investimentos.salvar(Investimento.novo(usuarioId, command.nome(), command.tipo(), command.contaOrigemId()));
    }

    public Investimento buscar(Long usuarioId, Long investimentoId) {
        return investimentos.buscarPorIdEUsuario(investimentoId, usuarioId)
                .orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
    }

    public Investimento atualizar(Long usuarioId, Long investimentoId, CriarCommand command) {
        Investimento atual = buscar(usuarioId, investimentoId);
        conta(command.contaOrigemId(), usuarioId);
        return investimentos.salvar(Investimento.reconstituir(investimentoId, usuarioId, command.nome(), command.tipo(), command.contaOrigemId(), atual.isAtivo()));
    }

    public Investimento inativar(Long usuarioId, Long investimentoId) {
        Investimento investimento = buscar(usuarioId, investimentoId);
        return investimentos.salvar(Investimento.reconstituir(investimento.getId(), usuarioId, investimento.getNome(), investimento.getTipo(), investimento.getContaOrigemId(), false));
    }

    public List<Investimento> listar(Long usuarioId) {
        return investimentos.listarPorUsuario(usuarioId);
    }

    public Pagina<Investimento> listar(Long usuarioId, Paginacao paginacao) {
        return investimentos.listarPorUsuario(usuarioId, paginacao);
    }

    public MovimentoInvestimento movimentar(Long usuarioId, MovimentoCommand command) {
        Investimento investimento = buscar(usuarioId, command.investimentoId());
        if (!investimento.isAtivo()) {
            throw new DomainException("error.investimento.inativo");
        }
        TipoTransacao tipo = command.tipo() == TipoMovimentoInvestimento.APORTE ? TipoTransacao.SAIDA : TipoTransacao.ENTRADA;
        Transacao transacao = transacoes.registrarTransacao(usuarioId, new FinanceiroCoreUseCase.RegistrarTransacaoCommand(
                tipo, command.valor(), command.data(), command.tipo() + " - " + investimento.getNome(),
                investimento.getContaOrigemId(), null, null, List.of()));
        return investimentos.salvarMovimento(MovimentoInvestimento.reconstituir(null, investimento.getId(), command.tipo(),
                ValorMonetario.of(command.valor()), command.data(), transacao.getId()));
    }

    public List<MovimentoInvestimento> listarMovimentos(Long usuarioId, Long investimentoId) {
        buscar(usuarioId, investimentoId);
        return investimentos.listarMovimentos(investimentoId);
    }

    public Pagina<MovimentoInvestimento> listarMovimentos(Long usuarioId, Long investimentoId, Paginacao paginacao) {
        buscar(usuarioId, investimentoId);
        return investimentos.listarMovimentos(investimentoId, paginacao);
    }

    private Conta conta(Long contaId, Long usuarioId) {
        return ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(contaId, usuarioId)
                .orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")));
    }
}
