package com.finisus.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.MeioPagamentoJpaEntity;
import com.finisus.adapters.out.persistence.repository.MeioPagamentoJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.MeioPagamentoRepositoryPort;
import com.finisus.domain.model.MeioPagamento;

@Component
public class MeioPagamentoPersistenceAdapter implements MeioPagamentoRepositoryPort {
	private final MeioPagamentoJpaRepository repository;

	public MeioPagamentoPersistenceAdapter(MeioPagamentoJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public MeioPagamento salvar(MeioPagamento meio) {
		MeioPagamentoJpaEntity e = new MeioPagamentoJpaEntity();
		e.setId(meio.getId());
		e.setUsuarioId(meio.getUsuarioId());
		e.setNome(meio.getNome());
		e.setAtivo(meio.isAtivo());
		return toDomain(repository.save(e));
	}

	@Override
	public Optional<MeioPagamento> buscarPorId(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<MeioPagamento> buscarPorIdEUsuario(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public List<MeioPagamento> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioIdAndAtivoTrue(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<MeioPagamento> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository.findByUsuarioIdAndAtivoTrue(usuarioId,
						PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))),
				this::toDomain);
	}

	private MeioPagamento toDomain(MeioPagamentoJpaEntity e) {
		return MeioPagamento.reconstituir(e.getId(), e.getUsuarioId(), e.getNome(), e.isAtivo());
	}
}
