package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.MeioPagamentoJpaEntity;
import com.financeiro.adapters.out.persistence.repository.MeioPagamentoJpaRepository;
import com.financeiro.application.ports.out.MeioPagamentoRepositoryPort;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.MeioPagamento;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

@Component
public class MeioPagamentoPersistenceAdapter implements MeioPagamentoRepositoryPort {
    private final MeioPagamentoJpaRepository repository;
    public MeioPagamentoPersistenceAdapter(MeioPagamentoJpaRepository repository) { this.repository = repository; }
    public MeioPagamento salvar(MeioPagamento meio) { MeioPagamentoJpaEntity e = new MeioPagamentoJpaEntity(); e.setId(meio.getId()); e.setUsuarioId(meio.getUsuarioId()); e.setNome(meio.getNome()); e.setAtivo(meio.isAtivo()); return toDomain(repository.save(e)); }
    public Optional<MeioPagamento> buscarPorId(Long id) { return repository.findById(id).map(this::toDomain); }
    public Optional<MeioPagamento> buscarPorIdEUsuario(Long id, Long usuarioId) { return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain); }
    public List<MeioPagamento> listarPorUsuario(Long usuarioId) { return repository.findByUsuarioIdAndAtivoTrue(usuarioId).stream().map(this::toDomain).toList(); }
    public Pagina<MeioPagamento> listarPorUsuario(Long usuarioId, Paginacao paginacao) { return PaginaJpaMapper.map(repository.findByUsuarioIdAndAtivoTrue(usuarioId, PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))), this::toDomain); }
    private MeioPagamento toDomain(MeioPagamentoJpaEntity e) { return MeioPagamento.reconstituir(e.getId(), e.getUsuarioId(), e.getNome(), e.isAtivo()); }
}
