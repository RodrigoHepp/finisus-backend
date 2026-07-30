package com.financeiro.application.ports.out;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Fatura;
import com.financeiro.domain.vo.AnoMes;
import java.util.List;
import java.util.Optional;
public interface FaturaRepositoryPort { Fatura salvar(Fatura fatura); Optional<Fatura> buscarPorCartaoEMes(Long cartaoId, AnoMes anoMes); Optional<Fatura> buscarPorId(Long id); Optional<Fatura> buscarPorIdParaAtualizacao(Long id); List<Fatura> listarPorCartao(Long cartaoId); Pagina<Fatura> listarPorCartao(Long cartaoId, Paginacao paginacao); }
