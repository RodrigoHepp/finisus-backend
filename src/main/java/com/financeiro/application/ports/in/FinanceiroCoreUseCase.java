package com.financeiro.application.ports.in;

import com.financeiro.domain.model.*;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinanceiroCoreUseCase {
    Banco criarBanco(Long usuarioId, CriarBancoCommand command);
    List<Banco> listarBancos(Long usuarioId);
    Pagina<Banco> listarBancos(Long usuarioId, Paginacao paginacao);
    Banco buscarBanco(Long usuarioId, Long bancoId);
    Banco atualizarBanco(Long usuarioId, Long bancoId, CriarBancoCommand command);
    Banco inativarBanco(Long usuarioId, Long bancoId);
    Conta criarConta(Long usuarioId, CriarContaCommand command);
    List<Conta> listarContas(Long usuarioId);
    Pagina<Conta> listarContas(Long usuarioId, Paginacao paginacao);
    Conta buscarConta(Long usuarioId, Long contaId);
    Conta atualizarConta(Long usuarioId, Long contaId, CriarContaCommand command);
    Conta inativarConta(Long usuarioId, Long contaId);
    Categoria criarCategoria(Long usuarioId, CriarCategoriaCommand command);
    List<Categoria> listarCategorias(Long usuarioId);
    Pagina<Categoria> listarCategorias(Long usuarioId, Paginacao paginacao);
    Categoria buscarCategoria(Long usuarioId, Long categoriaId);
    Categoria atualizarCategoria(Long usuarioId, Long categoriaId, CriarCategoriaCommand command);
    Categoria inativarCategoria(Long usuarioId, Long categoriaId);
    MeioPagamento criarMeioPagamento(Long usuarioId, CriarMeioPagamentoCommand command);
    List<MeioPagamento> listarMeiosPagamento(Long usuarioId);
    Pagina<MeioPagamento> listarMeiosPagamento(Long usuarioId, Paginacao paginacao);
    MeioPagamento buscarMeioPagamento(Long usuarioId, Long meioPagamentoId);
    MeioPagamento atualizarMeioPagamento(Long usuarioId, Long meioPagamentoId, CriarMeioPagamentoCommand command);
    MeioPagamento inativarMeioPagamento(Long usuarioId, Long meioPagamentoId);
    Transacao registrarTransacao(Long usuarioId, RegistrarTransacaoCommand command);
    List<Transacao> listarTransacoes(Long usuarioId);
    Pagina<Transacao> listarTransacoes(Long usuarioId, Paginacao paginacao);
    Transacao buscarTransacao(Long usuarioId, Long transacaoId);
    Transacao corrigirTransacao(Long usuarioId, Long transacaoId, RegistrarTransacaoCommand command);
    Transacao estornarTransacao(Long usuarioId, Long transacaoId);
    List<TransacaoHistorico> listarHistoricoTransacao(Long usuarioId, Long transacaoId);
    Pagina<TransacaoHistorico> listarHistoricoTransacao(Long usuarioId, Long transacaoId, Paginacao paginacao);

    record CriarBancoCommand(String nome, String codigo) {}
    record CriarContaCommand(String nome, TipoConta tipo, Long bancoId) {}
    record CriarCategoriaCommand(String nome, Long categoriaPaiId) {}
    record CriarMeioPagamentoCommand(String nome) {}
    record RegistrarTransacaoCommand(TipoTransacao tipo, BigDecimal valor, LocalDate data, String descricao,
                                    Long contaId, Long categoriaId, Long meioPagamentoId, List<ItemCommand> itens) {}
    record ItemCommand(String descricao, BigDecimal valor, Long categoriaId) {}
}
