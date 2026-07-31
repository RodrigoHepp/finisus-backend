package com.financeiro.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.financeiro.adapters.out.persistence.entity.ContaJpaEntity;
import com.financeiro.adapters.out.persistence.repository.ContaJpaRepository;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.domain.model.Conta;
import com.financeiro.domain.vo.ValorMonetario;

@Component
public class ContaPersistenceAdapter implements ContaRepositoryPort {
	private final ContaJpaRepository repository;

	public ContaPersistenceAdapter(ContaJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Conta salvar(Conta conta) {
		ContaJpaEntity e = new ContaJpaEntity();
		e.setId(conta.getId());
		e.setUsuarioId(conta.getUsuarioId());
		e.setNome(conta.getNome());
		e.setTipo(conta.getTipo());
		e.setBancoId(conta.getBancoId());
		e.setSaldo(conta.getSaldo().valor());
		e.setAtivo(conta.isAtivo());
		e.setVersion(conta.getVersion());
		return toDomain(repository.save(e));
	}

	@Override
	public Optional<Conta> buscarPorId(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Conta> buscarPorIdEUsuario(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public List<Conta> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<Conta> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository.findByUsuarioId(usuarioId,
						PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))),
				this::toDomain);
	}

	@Override
	public boolean existePorIdEUsuario(Long id, Long usuarioId) {
		return repository.existsByIdAndUsuarioId(id, usuarioId);
	}

	private Conta toDomain(ContaJpaEntity e) {
		return Conta.reconstituir(e.getId(), e.getUsuarioId(), e.getNome(), e.getTipo(), e.getBancoId(),
				ValorMonetario.of(e.getSaldo()), e.isAtivo(), e.getVersion() == null ? 0 : e.getVersion());
	}
}
