package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.TransacaoHistoricoJpaEntity;
import com.financeiro.adapters.out.persistence.entity.TransacaoItemJpaEntity;
import com.financeiro.adapters.out.persistence.entity.TransacaoJpaEntity;
import com.financeiro.adapters.out.persistence.repository.TransacaoHistoricoJpaRepository;
import com.financeiro.adapters.out.persistence.repository.TransacaoJpaRepository;
import com.financeiro.application.ports.out.TransacaoRepositoryPort;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Transacao;
import com.financeiro.domain.model.TransacaoHistorico;
import com.financeiro.domain.model.TransacaoItem;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class TransacaoPersistenceAdapter implements TransacaoRepositoryPort {
    private final TransacaoJpaRepository repository;
    private final TransacaoHistoricoJpaRepository historicoRepository;
    public TransacaoPersistenceAdapter(TransacaoJpaRepository repository, TransacaoHistoricoJpaRepository historicoRepository) {
        this.repository = repository; this.historicoRepository = historicoRepository;
    }
    public Transacao salvar(Transacao transacao) {
        TransacaoJpaEntity e = new TransacaoJpaEntity();
        e.setId(transacao.getId()); e.setUsuarioId(transacao.getUsuarioId()); e.setTipo(transacao.getTipo()); e.setValor(transacao.getValor().valor());
        e.setData(transacao.getData()); e.setDescricao(transacao.getDescricao()); e.setContaId(transacao.getContaId()); e.setCategoriaId(transacao.getCategoriaId());
        e.setMeioPagamentoId(transacao.getMeioPagamentoId()); e.setFaturaId(transacao.getFaturaId()); e.setCompraParceladaId(transacao.getCompraParceladaId());
        e.setRecorrenciaId(transacao.getRecorrenciaId()); e.setDespesaCompartilhadaId(transacao.getDespesaCompartilhadaId()); e.setEstornadoEm(transacao.getEstornadoEm()); e.setVersion(transacao.getVersion());
        for (TransacaoItem item : transacao.getItens()) {
            TransacaoItemJpaEntity itemEntity = new TransacaoItemJpaEntity();
            itemEntity.setId(item.getId()); itemEntity.setTransacao(e); itemEntity.setDescricao(item.getDescricao()); itemEntity.setValor(item.getValor().valor()); itemEntity.setCategoriaId(item.getCategoriaId());
            e.getItens().add(itemEntity);
        }
        return toDomain(repository.save(e));
    }
    @Transactional(readOnly = true) public Optional<Transacao> buscarPorId(Long id) { return repository.findById(id).map(this::toDomain); }
    @Transactional(readOnly = true) public Optional<Transacao> buscarPorIdEUsuario(Long id, Long usuarioId) { return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain); }
    @Transactional(readOnly = true) public List<Transacao> listarPorUsuario(Long usuarioId) { return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList(); }
    @Transactional(readOnly = true) public Pagina<Transacao> listarPorUsuario(Long usuarioId, Paginacao paginacao) { return PaginaJpaMapper.map(repository.findByUsuarioId(usuarioId, PaginaJpaMapper.pageable(paginacao, Sort.by("data").descending().and(Sort.by("id").descending()))), this::toDomain); }
    @Transactional(readOnly = true) public List<Transacao> listarPorFatura(Long faturaId) { return repository.findByFaturaId(faturaId).stream().map(this::toDomain).toList(); }
    @Transactional(readOnly = true) public List<Transacao> listarPorCompraParcelada(Long compraParceladaId) { return repository.findByCompraParceladaId(compraParceladaId).stream().map(this::toDomain).toList(); }
    @Transactional(readOnly = true) public List<Transacao> listarPorConta(Long contaId) { return repository.findByContaId(contaId).stream().map(this::toDomain).toList(); }
    public TransacaoHistorico salvarHistorico(TransacaoHistorico historico) {
        TransacaoHistoricoJpaEntity e = new TransacaoHistoricoJpaEntity(); e.setId(historico.getId()); e.setTransacaoId(historico.getTransacaoId());
        e.setCampoAlterado(historico.getCampoAlterado()); e.setValorAnterior(historico.getValorAnterior()); e.setValorNovo(historico.getValorNovo()); e.setAlteradoPor(historico.getAlteradoPor()); e.setAlteradoEm(historico.getAlteradoEm());
        return toDomain(historicoRepository.save(e));
    }
    @Transactional(readOnly = true) public List<TransacaoHistorico> listarHistoricoPorTransacao(Long transacaoId) { return historicoRepository.findByTransacaoIdOrderByAlteradoEmDesc(transacaoId).stream().map(this::toDomain).toList(); }
    @Transactional(readOnly = true) public Pagina<TransacaoHistorico> listarHistoricoPorTransacao(Long transacaoId, Paginacao paginacao) { return PaginaJpaMapper.map(historicoRepository.findByTransacaoId(transacaoId, PaginaJpaMapper.pageable(paginacao, Sort.by("alteradoEm").descending().and(Sort.by("id").descending()))), this::toDomain); }
    private Transacao toDomain(TransacaoJpaEntity e) {
        List<TransacaoItem> itens = e.getItens().stream().map(i -> TransacaoItem.reconstituir(i.getId(), i.getDescricao(), ValorMonetario.of(i.getValor()), i.getCategoriaId())).toList();
        return Transacao.reconstituir(e.getId(), e.getUsuarioId(), e.getTipo(), ValorMonetario.of(e.getValor()), e.getData(), e.getDescricao(), e.getContaId(), e.getCategoriaId(), e.getMeioPagamentoId(), e.getFaturaId(), e.getCompraParceladaId(), e.getRecorrenciaId(), e.getDespesaCompartilhadaId(), e.getEstornadoEm(), e.getVersion() == null ? 0 : e.getVersion(), itens);
    }
    private TransacaoHistorico toDomain(TransacaoHistoricoJpaEntity e) { return TransacaoHistorico.reconstituir(e.getId(), e.getTransacaoId(), e.getCampoAlterado(), e.getValorAnterior(), e.getValorNovo(), e.getAlteradoPor(), e.getAlteradoEm()); }
}
