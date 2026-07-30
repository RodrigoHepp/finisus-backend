package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.FinanciamentoJpaEntity;
import com.financeiro.adapters.out.persistence.entity.ParcelaFinanciamentoJpaEntity;
import com.financeiro.adapters.out.persistence.repository.FinanciamentoJpaRepository;
import com.financeiro.adapters.out.persistence.repository.ParcelaFinanciamentoJpaRepository;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.FinanciamentoRepositoryPort;
import com.financeiro.domain.model.Financiamento;
import com.financeiro.domain.model.ParcelaFinanciamento;
import com.financeiro.domain.model.StatusParcelaFinanciamento;
import com.financeiro.domain.ConflitoAtualizacaoException;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class FinanciamentoPersistenceAdapter implements FinanciamentoRepositoryPort {
    private final FinanciamentoJpaRepository repository;
    private final ParcelaFinanciamentoJpaRepository parcelas;

    public FinanciamentoPersistenceAdapter(FinanciamentoJpaRepository repository, ParcelaFinanciamentoJpaRepository parcelas) {
        this.repository = repository;
        this.parcelas = parcelas;
    }

    public Financiamento salvarComParcelas(Financiamento financiamento, List<ParcelaFinanciamento> values) {
        FinanciamentoJpaEntity entity = new FinanciamentoJpaEntity();
        entity.setUsuarioId(financiamento.getUsuarioId());
        entity.setDescricao(financiamento.getDescricao());
        entity.setPrincipal(financiamento.getPrincipal().valor());
        entity.setTaxaJurosMensal(financiamento.getTaxaJurosMensal());
        entity.setNumeroParcelas(financiamento.getNumeroParcelas());
        entity.setDataInicio(financiamento.getDataInicio());
        entity.setContaId(financiamento.getContaId());
        for (ParcelaFinanciamento parcela : values) {
            ParcelaFinanciamentoJpaEntity parcelaEntity = new ParcelaFinanciamentoJpaEntity();
            parcelaEntity.setFinanciamento(entity);
            parcelaEntity.setNumero(parcela.getNumero());
            parcelaEntity.setValor(parcela.getValor().valor());
            parcelaEntity.setDataVencimento(parcela.getDataVencimento());
            parcelaEntity.setStatus(parcela.getStatus());
            entity.getParcelas().add(parcelaEntity);
        }
        return toDomain(repository.save(entity));
    }

    public Optional<Financiamento> buscarPorIdEUsuario(Long id, Long usuarioId) { return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain); }
    public List<Financiamento> listarPorUsuario(Long usuarioId) { return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList(); }
    public Pagina<Financiamento> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
        return PaginaJpaMapper.map(repository.findByUsuarioId(usuarioId, PaginaJpaMapper.pageable(paginacao,
                Sort.by("dataInicio").descending().and(Sort.by("id").descending()))), this::toDomain);
    }
    public List<ParcelaFinanciamento> listarParcelas(Long financiamentoId) { return parcelas.findByFinanciamentoIdOrderByNumero(financiamentoId).stream().map(this::toDomain).toList(); }
    public Pagina<ParcelaFinanciamento> listarParcelas(Long financiamentoId, Paginacao paginacao) {
        return PaginaJpaMapper.map(parcelas.findByFinanciamentoId(financiamentoId, PaginaJpaMapper.pageable(paginacao,
                Sort.by("numero").ascending().and(Sort.by("id").ascending()))), this::toDomain);
    }
    public ParcelaFinanciamento salvarParcela(ParcelaFinanciamento parcela) {
        ParcelaFinanciamentoJpaEntity entity = parcelas.findById(parcela.getId()).orElseThrow();
        entity.setStatus(parcela.getStatus());
        try {
            return toDomain(parcelas.saveAndFlush(entity));
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ConflitoAtualizacaoException();
        }
    }
    public List<ParcelaFinanciamento> listarParcelasVencidas(LocalDate dataReferencia) {
        return parcelas.findByStatusAndDataVencimentoBefore(StatusParcelaFinanciamento.PENDENTE, dataReferencia)
                .stream().map(this::toDomain).toList();
    }
    public List<ParcelaFinanciamento> listarParcelasPorUsuarioEPeriodo(Long usuarioId, AnoMes inicio, AnoMes fim) {
        return parcelas.findRelevantByUsuarioIdAndPeriodo(usuarioId, inicio.primeiroDia(), fim.proximo().primeiroDia(), StatusParcelaFinanciamento.PAGA)
                .stream().map(this::toDomain).toList();
    }

    private Financiamento toDomain(FinanciamentoJpaEntity entity) { return Financiamento.reconstituir(entity.getId(), entity.getUsuarioId(), entity.getDescricao(), ValorMonetario.of(entity.getPrincipal()), entity.getTaxaJurosMensal(), entity.getNumeroParcelas(), entity.getDataInicio(), entity.getContaId()); }
    private ParcelaFinanciamento toDomain(ParcelaFinanciamentoJpaEntity entity) { return ParcelaFinanciamento.reconstituir(entity.getId(), entity.getFinanciamento().getId(), entity.getNumero(), ValorMonetario.of(entity.getValor()), entity.getDataVencimento(), entity.getStatus()); }
}
