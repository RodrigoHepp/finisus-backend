package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.InvestimentoJpaEntity;
import com.financeiro.adapters.out.persistence.entity.MovimentoInvestimentoJpaEntity;
import com.financeiro.adapters.out.persistence.repository.InvestimentoJpaRepository;
import com.financeiro.adapters.out.persistence.repository.MovimentoInvestimentoJpaRepository;
import com.financeiro.application.ports.out.InvestimentoRepositoryPort;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.*;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

@Component
public class InvestimentoPersistenceAdapter implements InvestimentoRepositoryPort {
    private final InvestimentoJpaRepository investimentos; private final MovimentoInvestimentoJpaRepository movimentos;
    public InvestimentoPersistenceAdapter(InvestimentoJpaRepository investimentos, MovimentoInvestimentoJpaRepository movimentos) { this.investimentos = investimentos; this.movimentos = movimentos; }
    public Investimento salvar(Investimento investimento) { InvestimentoJpaEntity entity = new InvestimentoJpaEntity(); entity.setId(investimento.getId()); entity.setUsuarioId(investimento.getUsuarioId()); entity.setNome(investimento.getNome()); entity.setTipo(investimento.getTipo()); entity.setContaOrigemId(investimento.getContaOrigemId()); entity.setAtivo(investimento.isAtivo()); return toDomain(investimentos.save(entity)); }
    public Optional<Investimento> buscarPorIdEUsuario(Long id, Long usuarioId) { return investimentos.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain); }
    public List<Investimento> listarPorUsuario(Long usuarioId) { return investimentos.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList(); }
    public Pagina<Investimento> listarPorUsuario(Long usuarioId, Paginacao paginacao) { return PaginaJpaMapper.map(investimentos.findByUsuarioId(usuarioId, PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))), this::toDomain); }
    public MovimentoInvestimento salvarMovimento(MovimentoInvestimento movimento) { MovimentoInvestimentoJpaEntity entity = new MovimentoInvestimentoJpaEntity(); entity.setId(movimento.getId()); entity.setInvestimentoId(movimento.getInvestimentoId()); entity.setTipo(movimento.getTipo()); entity.setValor(movimento.getValor().valor()); entity.setData(movimento.getData()); entity.setTransacaoId(movimento.getTransacaoId()); return toDomain(movimentos.save(entity)); }
    public List<MovimentoInvestimento> listarMovimentos(Long investimentoId) { return movimentos.findByInvestimentoIdOrderByDataDesc(investimentoId).stream().map(this::toDomain).toList(); }
    public Pagina<MovimentoInvestimento> listarMovimentos(Long investimentoId, Paginacao paginacao) { return PaginaJpaMapper.map(movimentos.findByInvestimentoId(investimentoId, PaginaJpaMapper.pageable(paginacao, Sort.by("data").descending().and(Sort.by("id").descending()))), this::toDomain); }
    private Investimento toDomain(InvestimentoJpaEntity entity) { return Investimento.reconstituir(entity.getId(), entity.getUsuarioId(), entity.getNome(), entity.getTipo(), entity.getContaOrigemId(), entity.isAtivo()); }
    private MovimentoInvestimento toDomain(MovimentoInvestimentoJpaEntity entity) { return MovimentoInvestimento.reconstituir(entity.getId(), entity.getInvestimentoId(), entity.getTipo(), ValorMonetario.of(entity.getValor()), entity.getData(), entity.getTransacaoId()); }
}
