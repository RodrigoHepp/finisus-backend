package com.financeiro.application.service;

import com.financeiro.application.ports.in.FinanciamentoUseCase;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.application.ports.out.FinanciamentoRepositoryPort;
import com.financeiro.application.ports.out.TransacaoRepositoryPort;
import com.financeiro.application.ports.out.ObterDataAtualPort;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Financiamento;
import com.financeiro.domain.model.ParcelaFinanciamento;
import com.financeiro.domain.model.Conta;
import com.financeiro.domain.model.Transacao;
import com.financeiro.domain.model.TransacaoHistorico;
import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.vo.ValorMonetario;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class FinanciamentoService implements FinanciamentoUseCase {
    private final FinanciamentoRepositoryPort financiamentos;
    private final ContaRepositoryPort contas;
    private final TransacaoRepositoryPort transacoes;
    private final ObterDataAtualPort dataAtual;

    public FinanciamentoService(FinanciamentoRepositoryPort financiamentos, ContaRepositoryPort contas, TransacaoRepositoryPort transacoes, ObterDataAtualPort dataAtual) {
        this.financiamentos = financiamentos;
        this.contas = contas;
        this.transacoes = transacoes;
        this.dataAtual = dataAtual;
    }

    @Override
    public Financiamento criar(Long usuarioId, CriarCommand command) {
        if (command.numeroParcelas() < 1 || command.taxaJurosMensal().signum() < 0) {
            throw new DomainException("error.financiamento.invalido");
        }
        ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)
                .orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")));
        Financiamento financiamento = Financiamento.novo(usuarioId, command.descricao(),
                ValorMonetario.of(command.principal()), command.taxaJurosMensal(),
                command.numeroParcelas(), command.dataInicio(), command.contaId());

        // A taxa recebida é mensal e em percentual, por exemplo: 1.50 = 1,5% a.m.
        BigDecimal taxa = command.taxaJurosMensal().movePointLeft(2);
        BigDecimal valorParcela = taxa.signum() == 0
                ? command.principal().divide(BigDecimal.valueOf(command.numeroParcelas()), 2, RoundingMode.HALF_UP)
                : command.principal().multiply(taxa).multiply(BigDecimal.ONE.add(taxa).pow(command.numeroParcelas()))
                .divide(BigDecimal.ONE.add(taxa).pow(command.numeroParcelas()).subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        List<ParcelaFinanciamento> parcelas = java.util.stream.IntStream.rangeClosed(1, command.numeroParcelas())
                .mapToObj(numero -> ParcelaFinanciamento.nova(null, numero, ValorMonetario.of(valorParcela), command.dataInicio().plusMonths(numero - 1)))
                .toList();
        return financiamentos.salvarComParcelas(financiamento, parcelas);
    }

    @Override
    public List<Financiamento> listar(Long usuarioId) {
        return financiamentos.listarPorUsuario(usuarioId);
    }
    @Override
    public Pagina<Financiamento> listar(Long usuarioId, Paginacao paginacao) {
        return financiamentos.listarPorUsuario(usuarioId, paginacao);
    }
    @Override public Financiamento buscar(Long usuarioId,Long financiamentoId){return financiamentos.buscarPorIdEUsuario(financiamentoId,usuarioId).orElseThrow(()->new DomainException("error.recurso.nao.encontrado"));}
    @Override public List<ParcelaFinanciamento> listarParcelas(Long usuarioId,Long financiamentoId){buscar(usuarioId,financiamentoId);return financiamentos.listarParcelas(financiamentoId);}
    @Override public Pagina<ParcelaFinanciamento> listarParcelas(Long usuarioId,Long financiamentoId,Paginacao paginacao){buscar(usuarioId,financiamentoId);return financiamentos.listarParcelas(financiamentoId,paginacao);}
    @Override public ParcelaFinanciamento pagarParcela(Long usuarioId,Long financiamentoId,Long parcelaId,java.time.LocalDate dataPagamento){Financiamento financiamento=buscar(usuarioId,financiamentoId);ParcelaFinanciamento parcela=listarParcelas(usuarioId,financiamentoId).stream().filter(p->p.getId().equals(parcelaId)).findFirst().orElseThrow(()->new DomainException("error.recurso.nao.encontrado"));Conta conta=ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(financiamento.getContaId(),usuarioId).orElseThrow(()->new DomainException("error.recurso.nao.encontrado")));Transacao pagamento=transacoes.salvar(Transacao.nova(usuarioId,TipoTransacao.SAIDA,parcela.getValor(),dataPagamento,"Parcela "+parcela.getNumero()+" - "+financiamento.getDescricao(),conta.getId(),null,null,List.of()));contas.salvar(Conta.reconstituir(conta.getId(),conta.getUsuarioId(),conta.getNome(),conta.getTipo(),conta.getBancoId(),conta.getSaldo().subtrair(parcela.getValor()),conta.isAtivo(),conta.getVersion()));transacoes.salvarHistorico(TransacaoHistorico.registrar(pagamento.getId(),"PAGAMENTO_PARCELA",null,parcela.getValor().valor().toPlainString(),usuarioId,dataAtual.obterDataHora()));parcela.pagar();return financiamentos.salvarParcela(parcela);}
    @Override public int processarAtrasos(ProcessarAtrasosCommand command){
        java.time.LocalDate dataReferencia = command.dataReferencia();
        List<ParcelaFinanciamento> vencidas = financiamentos.listarParcelasVencidas(dataReferencia);
        vencidas.forEach(parcela -> {
            parcela.marcarAtrasada(dataReferencia);
            financiamentos.salvarParcela(parcela);
        });
        return vencidas.size();
    }
}
