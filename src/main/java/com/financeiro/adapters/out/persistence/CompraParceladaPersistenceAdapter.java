package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.CompraParceladaJpaEntity;
import com.financeiro.adapters.out.persistence.repository.CompraParceladaJpaRepository;
import com.financeiro.application.ports.out.CompraParceladaRepositoryPort;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.CompraParcelada;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

@Component
public class CompraParceladaPersistenceAdapter implements CompraParceladaRepositoryPort {
    private final CompraParceladaJpaRepository repository;
    public CompraParceladaPersistenceAdapter(CompraParceladaJpaRepository repository) { this.repository = repository; }
    public CompraParcelada salvar(CompraParcelada compra) { CompraParceladaJpaEntity e = new CompraParceladaJpaEntity(); e.setId(compra.getId()); e.setUsuarioId(compra.getUsuarioId()); e.setDescricao(compra.getDescricao()); e.setValorTotal(compra.getValorTotal().valor()); e.setNumeroParcelas(compra.getNumeroParcelas()); e.setDataCompra(compra.getDataCompra()); e.setCategoriaId(compra.getCategoriaId()); e.setContaId(compra.getContaId()); e.setCanceladaEm(compra.getCanceladaEm()); return toDomain(repository.save(e)); }
    public Optional<CompraParcelada> buscarPorId(Long id) { return repository.findById(id).map(this::toDomain); }
    public Optional<CompraParcelada> buscarPorIdEUsuario(Long id, Long usuarioId) { return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain); }
    public List<CompraParcelada> listarPorUsuario(Long usuarioId) { return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList(); }
    public Pagina<CompraParcelada> listarPorUsuario(Long usuarioId, Paginacao paginacao) { return PaginaJpaMapper.map(repository.findByUsuarioId(usuarioId, PaginaJpaMapper.pageable(paginacao, Sort.by("dataCompra").descending().and(Sort.by("id").descending()))), this::toDomain); }
    private CompraParcelada toDomain(CompraParceladaJpaEntity e) { return CompraParcelada.reconstituir(e.getId(), e.getUsuarioId(), e.getDescricao(), ValorMonetario.of(e.getValorTotal()), e.getNumeroParcelas(), e.getDataCompra(), e.getCategoriaId(), e.getContaId(), e.getCanceladaEm()); }
}
