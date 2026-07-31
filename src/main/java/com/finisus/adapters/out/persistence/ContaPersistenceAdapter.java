package com.finisus.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.ContaJpaEntity;
import com.finisus.adapters.out.persistence.repository.ContaJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.domain.model.Conta;
import com.finisus.domain.vo.ValorMonetario;

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
