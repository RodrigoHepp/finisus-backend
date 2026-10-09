package com.finisus.adapters.out.persistence;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.UsuarioJpaEntity;
import com.finisus.adapters.out.persistence.repository.UsuarioJpaRepository;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.vo.Email;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {
	private final UsuarioJpaRepository repository;

	public UsuarioPersistenceAdapter(UsuarioJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Usuario salvar(Usuario usuario) {
		UsuarioJpaEntity entity = new UsuarioJpaEntity();
		entity.setId(usuario.getId());
		entity.setNome(usuario.getNome());
		entity.setEmail(usuario.getEmail().valor());
		entity.setSenhaHash(usuario.getSenhaHash());
		entity.setAtivo(usuario.isAtivo());
		entity.setCriadoEm(usuario.getCriadoEm());
		entity.setSessaoVersao(usuario.getSessaoVersao());
		entity.setTentativasLoginInvalidas(usuario.getTentativasLoginInvalidas());
		entity.setBloqueado(usuario.isBloqueado());
		entity.setPermissoes(new HashSet<>(usuario.getPermissoes()));
		return toDomain(repository.save(entity));
	}

	@Override
	public Optional<Usuario> buscarPorId(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Usuario> buscarPorIdParaAtualizacao(Long id) {
		return repository.findByIdForUpdate(id).map(this::toDomain);
	}

	@Override
	public List<Usuario> buscarPorIds(Collection<Long> ids) {
		return repository.findByIdIn(ids).stream().map(this::toDomain).toList();
	}

	@Override
	public Optional<Usuario> buscarPorEmail(String email) {
		return repository.findByEmail(email).map(this::toDomain);
	}

	@Override
	public Optional<Usuario> buscarPorEmailParaAtualizacao(String email) {
		return repository.findByEmailForUpdate(email).map(this::toDomain);
	}

	@Override
	public boolean existePorEmail(String email) {
		return repository.existsByEmail(email);
	}

	@Override
	public List<Usuario> listarAtivos() {
		return repository.findByAtivoTrue().stream().map(this::toDomain).toList();
	}

	private Usuario toDomain(UsuarioJpaEntity e) {
		return Usuario.reconstituir(e.getId(), e.getNome(), new Email(e.getEmail()), e.getSenhaHash(), e.isAtivo(),
				e.getCriadoEm(), e.getSessaoVersao(), e.getTentativasLoginInvalidas(), e.isBloqueado(),
				e.getPermissoes());
	}
}
