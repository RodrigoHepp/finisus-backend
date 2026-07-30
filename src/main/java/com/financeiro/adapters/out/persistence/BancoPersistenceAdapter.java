package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.BancoJpaEntity;
import com.financeiro.adapters.out.persistence.repository.BancoJpaRepository;
import com.financeiro.application.ports.out.BancoRepositoryPort;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Banco;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

@Component
public class BancoPersistenceAdapter implements BancoRepositoryPort {
    private final BancoJpaRepository repository;
    public BancoPersistenceAdapter(BancoJpaRepository repository) { this.repository = repository; }
    public Banco salvar(Banco banco) { BancoJpaEntity e = new BancoJpaEntity(); e.setId(banco.getId()); e.setNome(banco.getNome()); e.setCodigo(banco.getCodigo()); e.setAtivo(banco.isAtivo()); e.setUsuarioId(banco.getUsuarioId()); return toDomain(repository.save(e)); }
    public Optional<Banco> buscarPorId(Long id) { return repository.findById(id).map(this::toDomain); }
    public List<Banco> listarSistema() { return repository.findByUsuarioIdIsNullAndAtivoTrue().stream().map(this::toDomain).toList(); }
    public List<Banco> listarPorUsuario(Long usuarioId) { return repository.findByUsuarioIdAndAtivoTrue(usuarioId).stream().map(this::toDomain).toList(); }
    public List<Banco> listarDisponiveisParaUsuario(Long usuarioId) { return repository.findByUsuarioIdIsNullOrUsuarioId(usuarioId).stream().filter(BancoJpaEntity::isAtivo).map(this::toDomain).toList(); }
    public Pagina<Banco> listarDisponiveisParaUsuario(Long usuarioId, Paginacao paginacao) { return PaginaJpaMapper.map(repository.findDisponiveisParaUsuario(usuarioId, PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))), this::toDomain); }
    private Banco toDomain(BancoJpaEntity e) { return Banco.reconstituir(e.getId(), e.getNome(), e.getCodigo(), e.isAtivo(), e.getUsuarioId()); }
}
