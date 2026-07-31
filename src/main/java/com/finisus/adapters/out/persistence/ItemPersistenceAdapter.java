package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.ItemJpaEntity;
import com.finisus.adapters.out.persistence.repository.ItemJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.ItemRepositoryPort;
import com.finisus.domain.model.Item;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ItemPersistenceAdapter implements ItemRepositoryPort {
	private final ItemJpaRepository repository;

	public ItemPersistenceAdapter(ItemJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Item salvar(Item item) {
		ItemJpaEntity entity = new ItemJpaEntity();
		entity.setId(item.getId());
		entity.setUsuarioId(item.getUsuarioId());
		entity.setNome(item.getNome());
		entity.setCategoriaPadraoId(item.getCategoriaPadraoId());
		entity.setAtivo(item.isAtivo());
		return toDomain(repository.save(entity));
	}

	@Override
	public Optional<Item> buscarPorIdEUsuario(Long itemId, Long usuarioId) {
		return repository.findByIdAndUsuarioId(itemId, usuarioId).map(this::toDomain);
	}

	@Override
	public Pagina<Item> listarAtivosPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository.findByUsuarioIdAndAtivoTrue(usuarioId,
						PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))),
				this::toDomain);
	}

	private Item toDomain(ItemJpaEntity entity) {
		return Item.reconstituir(entity.getId(), entity.getUsuarioId(), entity.getNome(), entity.getCategoriaPadraoId(),
				entity.isAtivo());
	}
}
