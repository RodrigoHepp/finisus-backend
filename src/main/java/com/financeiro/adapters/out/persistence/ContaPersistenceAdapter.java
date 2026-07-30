package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.ContaJpaEntity;
import com.financeiro.adapters.out.persistence.repository.ContaJpaRepository;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Conta;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

@Component
public class ContaPersistenceAdapter implements ContaRepositoryPort {
    private final ContaJpaRepository repository;
    public ContaPersistenceAdapter(ContaJpaRepository repository) { this.repository = repository; }
    public Conta salvar(Conta conta) { ContaJpaEntity e = new ContaJpaEntity(); e.setId(conta.getId()); e.setUsuarioId(conta.getUsuarioId()); e.setNome(conta.getNome()); e.setTipo(conta.getTipo()); e.setBancoId(conta.getBancoId()); e.setSaldo(conta.getSaldo().valor()); e.setAtivo(conta.isAtivo()); e.setVersion(conta.getVersion()); return toDomain(repository.save(e)); }
    public Optional<Conta> buscarPorId(Long id) { return repository.findById(id).map(this::toDomain); }
    public Optional<Conta> buscarPorIdEUsuario(Long id, Long usuarioId) { return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain); }
    public List<Conta> listarPorUsuario(Long usuarioId) { return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList(); }
    public Pagina<Conta> listarPorUsuario(Long usuarioId, Paginacao paginacao) { return PaginaJpaMapper.map(repository.findByUsuarioId(usuarioId, PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))), this::toDomain); }
    public boolean existePorIdEUsuario(Long id, Long usuarioId) { return repository.existsByIdAndUsuarioId(id, usuarioId); }
    private Conta toDomain(ContaJpaEntity e) { return Conta.reconstituir(e.getId(), e.getUsuarioId(), e.getNome(), e.getTipo(), e.getBancoId(), ValorMonetario.of(e.getSaldo()), e.isAtivo(), e.getVersion() == null ? 0 : e.getVersion()); }
}
