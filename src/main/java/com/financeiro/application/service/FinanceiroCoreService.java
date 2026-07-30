package com.financeiro.application.service;

import com.financeiro.application.ports.in.FinanceiroCoreUseCase;
import com.financeiro.application.ports.out.*;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.*;
import com.financeiro.domain.vo.ValorMonetario;

import java.util.List;

public class FinanceiroCoreService implements FinanceiroCoreUseCase {
    private final BancoRepositoryPort bancoRepository;
    private final ContaRepositoryPort contaRepository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final MeioPagamentoRepositoryPort meioPagamentoRepository;
    private final TransacaoRepositoryPort transacaoRepository;
    private final ObterDataAtualPort dataAtual;

    public FinanceiroCoreService(BancoRepositoryPort bancoRepository, ContaRepositoryPort contaRepository,
                                 CategoriaRepositoryPort categoriaRepository, MeioPagamentoRepositoryPort meioPagamentoRepository,
                                 TransacaoRepositoryPort transacaoRepository, ObterDataAtualPort dataAtual) {
        this.bancoRepository = bancoRepository; this.contaRepository = contaRepository; this.categoriaRepository = categoriaRepository;
        this.meioPagamentoRepository = meioPagamentoRepository; this.transacaoRepository = transacaoRepository; this.dataAtual = dataAtual;
    }
    public Banco criarBanco(Long usuarioId, CriarBancoCommand command) { return bancoRepository.salvar(Banco.novoUsuario(command.nome(), command.codigo(), usuarioId)); }
    public List<Banco> listarBancos(Long usuarioId) { return bancoRepository.listarDisponiveisParaUsuario(usuarioId); }
    public Pagina<Banco> listarBancos(Long usuarioId, Paginacao paginacao) { return bancoRepository.listarDisponiveisParaUsuario(usuarioId, paginacao); }
    public Banco buscarBanco(Long usuarioId, Long bancoId) { Banco banco = require(bancoRepository.buscarPorId(bancoId)); if (banco.isSistema() || !usuarioId.equals(banco.getUsuarioId())) throw notFound(); return banco; }
    public Banco atualizarBanco(Long usuarioId, Long bancoId, CriarBancoCommand command) { Banco banco = buscarBanco(usuarioId, bancoId); return bancoRepository.salvar(Banco.reconstituir(banco.getId(), command.nome(), command.codigo(), banco.isAtivo(), usuarioId)); }
    public Banco inativarBanco(Long usuarioId, Long bancoId) { Banco banco = buscarBanco(usuarioId, bancoId); return bancoRepository.salvar(Banco.reconstituir(banco.getId(), banco.getNome(), banco.getCodigo(), false, usuarioId)); }
    public Conta criarConta(Long usuarioId, CriarContaCommand command) {
        if (command.bancoId() != null && bancoRepository.buscarPorId(command.bancoId()).filter(b -> b.isSistema() || usuarioId.equals(b.getUsuarioId())).isEmpty()) throw notFound();
        return contaRepository.salvar(Conta.nova(usuarioId, command.nome(), command.tipo(), command.bancoId()));
    }
    public List<Conta> listarContas(Long usuarioId) { return contaRepository.listarPorUsuario(usuarioId); }
    public Pagina<Conta> listarContas(Long usuarioId, Paginacao paginacao) { return contaRepository.listarPorUsuario(usuarioId, paginacao); }
    public Conta buscarConta(Long usuarioId, Long contaId) { return require(contaRepository.buscarPorIdEUsuario(contaId, usuarioId)); }
    public Conta atualizarConta(Long usuarioId, Long contaId, CriarContaCommand command) { Conta conta = buscarConta(usuarioId, contaId); validarBanco(usuarioId, command.bancoId()); return contaRepository.salvar(Conta.reconstituir(conta.getId(), usuarioId, command.nome(), command.tipo(), command.bancoId(), conta.getSaldo(), conta.isAtivo(), conta.getVersion())); }
    public Conta inativarConta(Long usuarioId, Long contaId) { Conta conta = buscarConta(usuarioId, contaId); return contaRepository.salvar(Conta.reconstituir(conta.getId(), usuarioId, conta.getNome(), conta.getTipo(), conta.getBancoId(), conta.getSaldo(), false, conta.getVersion())); }
    public Categoria criarCategoria(Long usuarioId, CriarCategoriaCommand command) {
        if (command.categoriaPaiId() != null) require(categoriaRepository.buscarPorIdEUsuario(command.categoriaPaiId(), usuarioId));
        return categoriaRepository.salvar(Categoria.nova(usuarioId, command.nome(), command.categoriaPaiId()));
    }
    public List<Categoria> listarCategorias(Long usuarioId) { return categoriaRepository.listarPorUsuario(usuarioId); }
    public Pagina<Categoria> listarCategorias(Long usuarioId, Paginacao paginacao) { return categoriaRepository.listarPorUsuario(usuarioId, paginacao); }
    public Categoria buscarCategoria(Long usuarioId, Long categoriaId) { return require(categoriaRepository.buscarPorIdEUsuario(categoriaId, usuarioId)); }
    public Categoria atualizarCategoria(Long usuarioId, Long categoriaId, CriarCategoriaCommand command) { Categoria categoria = buscarCategoria(usuarioId, categoriaId); if (command.categoriaPaiId() != null && command.categoriaPaiId().equals(categoriaId)) throw new DomainException("error.categoria.pai.invalida"); if (command.categoriaPaiId() != null) require(categoriaRepository.buscarPorIdEUsuario(command.categoriaPaiId(), usuarioId)); return categoriaRepository.salvar(Categoria.reconstituir(categoriaId, usuarioId, command.nome(), command.categoriaPaiId(), categoria.isAtivo())); }
    public Categoria inativarCategoria(Long usuarioId, Long categoriaId) { Categoria categoria = buscarCategoria(usuarioId, categoriaId); return categoriaRepository.salvar(Categoria.reconstituir(categoriaId, usuarioId, categoria.getNome(), categoria.getCategoriaPaiId(), false)); }
    public MeioPagamento criarMeioPagamento(Long usuarioId, CriarMeioPagamentoCommand command) { return meioPagamentoRepository.salvar(MeioPagamento.novo(usuarioId, command.nome())); }
    public List<MeioPagamento> listarMeiosPagamento(Long usuarioId) { return meioPagamentoRepository.listarPorUsuario(usuarioId); }
    public Pagina<MeioPagamento> listarMeiosPagamento(Long usuarioId, Paginacao paginacao) { return meioPagamentoRepository.listarPorUsuario(usuarioId, paginacao); }
    public MeioPagamento buscarMeioPagamento(Long usuarioId, Long meioPagamentoId) { return require(meioPagamentoRepository.buscarPorIdEUsuario(meioPagamentoId, usuarioId)); }
    public MeioPagamento atualizarMeioPagamento(Long usuarioId, Long meioPagamentoId, CriarMeioPagamentoCommand command) { MeioPagamento meio = buscarMeioPagamento(usuarioId, meioPagamentoId); return meioPagamentoRepository.salvar(MeioPagamento.reconstituir(meio.getId(), usuarioId, command.nome(), meio.isAtivo())); }
    public MeioPagamento inativarMeioPagamento(Long usuarioId, Long meioPagamentoId) { MeioPagamento meio = buscarMeioPagamento(usuarioId, meioPagamentoId); return meioPagamentoRepository.salvar(MeioPagamento.reconstituir(meio.getId(), usuarioId, meio.getNome(), false)); }
    public Transacao registrarTransacao(Long usuarioId, RegistrarTransacaoCommand command) {
        Conta conta = ContaAtivaValidator.exigirAtiva(require(contaRepository.buscarPorIdEUsuario(command.contaId(), usuarioId)));
        if (command.categoriaId() != null) require(categoriaRepository.buscarPorIdEUsuario(command.categoriaId(), usuarioId));
        if (command.meioPagamentoId() != null) require(meioPagamentoRepository.buscarPorIdEUsuario(command.meioPagamentoId(), usuarioId));
        List<TransacaoItem> itens = command.itens() == null ? List.of() : command.itens().stream().map(item -> {
            if (item.categoriaId() != null) require(categoriaRepository.buscarPorIdEUsuario(item.categoriaId(), usuarioId));
            return TransacaoItem.novo(item.descricao(), ValorMonetario.of(item.valor()), item.categoriaId());
        }).toList();
        Transacao transacao = transacaoRepository.salvar(Transacao.nova(usuarioId, command.tipo(), ValorMonetario.of(command.valor()), command.data(), command.descricao(), command.contaId(), command.categoriaId(), command.meioPagamentoId(), itens));
        Conta atualizada = Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(), conta.getBancoId(),
                command.tipo() == TipoTransacao.ENTRADA ? conta.getSaldo().somar(transacao.getValor()) : conta.getSaldo().subtrair(transacao.getValor()), conta.getVersion());
        contaRepository.salvar(atualizada);
        transacaoRepository.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "CRIACAO", null, transacao.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
        return transacao;
    }
    public List<Transacao> listarTransacoes(Long usuarioId) { return transacaoRepository.listarPorUsuario(usuarioId); }
    public Pagina<Transacao> listarTransacoes(Long usuarioId, Paginacao paginacao) { return transacaoRepository.listarPorUsuario(usuarioId, paginacao); }
    public Transacao buscarTransacao(Long usuarioId, Long transacaoId) { return require(transacaoRepository.buscarPorIdEUsuario(transacaoId, usuarioId)); }
    public Transacao corrigirTransacao(Long usuarioId, Long transacaoId, RegistrarTransacaoCommand command) {
        Transacao anterior = buscarTransacao(usuarioId, transacaoId); Conta contaAnterior = require(contaRepository.buscarPorIdEUsuario(anterior.getContaId(), usuarioId)); Conta contaNova = require(contaRepository.buscarPorIdEUsuario(command.contaId(), usuarioId));
        if (command.categoriaId() != null) require(categoriaRepository.buscarPorIdEUsuario(command.categoriaId(), usuarioId)); if (command.meioPagamentoId() != null) require(meioPagamentoRepository.buscarPorIdEUsuario(command.meioPagamentoId(), usuarioId));
        List<TransacaoItem> itens = itens(usuarioId, command.itens()); Transacao corrigida = transacaoRepository.salvar(anterior.corrigida(command.tipo(), ValorMonetario.of(command.valor()), command.data(), command.descricao(), command.contaId(), command.categoriaId(), command.meioPagamentoId(), itens));
        ValorMonetario saldoRevertido = anterior.getTipo() == TipoTransacao.ENTRADA ? contaAnterior.getSaldo().subtrair(anterior.getValor()) : contaAnterior.getSaldo().somar(anterior.getValor());
        ValorMonetario saldoAplicado = command.tipo() == TipoTransacao.ENTRADA ? saldoRevertido.somar(corrigida.getValor()) : saldoRevertido.subtrair(corrigida.getValor());
        if (contaAnterior.getId().equals(contaNova.getId())) contaRepository.salvar(comSaldo(contaAnterior, saldoAplicado));
        else { contaRepository.salvar(comSaldo(contaAnterior, saldoRevertido)); contaRepository.salvar(comSaldo(contaNova, command.tipo() == TipoTransacao.ENTRADA ? contaNova.getSaldo().somar(corrigida.getValor()) : contaNova.getSaldo().subtrair(corrigida.getValor()))); }
        transacaoRepository.salvarHistorico(TransacaoHistorico.registrar(transacaoId, "CORRECAO", anterior.getValor().valor().toPlainString(), corrigida.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora())); return corrigida;
    }
    public Transacao estornarTransacao(Long usuarioId, Long transacaoId) {
        Transacao transacao = buscarTransacao(usuarioId, transacaoId);
        Transacao estornada = transacaoRepository.salvar(transacao.estornada(dataAtual.obterDataHora()));
        if (transacao.impactaSaldoDaConta()) {
            Conta conta = require(contaRepository.buscarPorIdEUsuario(transacao.getContaId(), usuarioId));
            ValorMonetario saldo = transacao.getTipo() == TipoTransacao.ENTRADA
                    ? conta.getSaldo().subtrair(transacao.getValor())
                    : conta.getSaldo().somar(transacao.getValor());
            contaRepository.salvar(comSaldo(conta, saldo));
        }
        transacaoRepository.salvarHistorico(TransacaoHistorico.registrar(transacaoId, "ESTORNO", transacao.getValor().valor().toPlainString(), null, usuarioId, dataAtual.obterDataHora()));
        return estornada;
    }
    public List<TransacaoHistorico> listarHistoricoTransacao(Long usuarioId, Long transacaoId) { buscarTransacao(usuarioId, transacaoId); return transacaoRepository.listarHistoricoPorTransacao(transacaoId); }
    public Pagina<TransacaoHistorico> listarHistoricoTransacao(Long usuarioId, Long transacaoId, Paginacao paginacao) { buscarTransacao(usuarioId, transacaoId); return transacaoRepository.listarHistoricoPorTransacao(transacaoId, paginacao); }
    private void validarBanco(Long usuarioId, Long bancoId) { if (bancoId != null && bancoRepository.buscarPorId(bancoId).filter(b -> b.isSistema() || usuarioId.equals(b.getUsuarioId())).isEmpty()) throw notFound(); }
    private List<TransacaoItem> itens(Long usuarioId, List<ItemCommand> commands) { return commands == null ? List.of() : commands.stream().map(item -> { if (item.categoriaId() != null) require(categoriaRepository.buscarPorIdEUsuario(item.categoriaId(), usuarioId)); return TransacaoItem.novo(item.descricao(), ValorMonetario.of(item.valor()), item.categoriaId()); }).toList(); }
    private Conta comSaldo(Conta conta, ValorMonetario saldo) { return Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(), conta.getBancoId(), saldo, conta.isAtivo(), conta.getVersion()); }
    private <T> T require(java.util.Optional<T> resource) { return resource.orElseThrow(this::notFound); }
    private DomainException notFound() { return new DomainException("error.recurso.nao.encontrado"); }
}
