package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.CartaoCreditoJpaEntity;
import com.financeiro.adapters.out.persistence.repository.CartaoCreditoJpaRepository;
import com.financeiro.application.ports.out.CartaoCreditoRepositoryPort;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.CartaoCredito;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Component
public class CartaoCreditoPersistenceAdapter implements CartaoCreditoRepositoryPort {
    private final CartaoCreditoJpaRepository repository;
    public CartaoCreditoPersistenceAdapter(CartaoCreditoJpaRepository repository) { this.repository = repository; }
    public CartaoCredito salvar(CartaoCredito card) { CartaoCreditoJpaEntity entity = new CartaoCreditoJpaEntity(); entity.setId(card.getId()); entity.setUsuarioId(card.getUsuarioId()); entity.setNome(card.getNome()); entity.setLimite(card.getLimite().valor()); entity.setDiaFechamento(card.getDiaFechamento()); entity.setDiaVencimento(card.getDiaVencimento()); entity.setAtivo(card.isAtivo()); return toDomain(repository.save(entity)); }
    public Optional<CartaoCredito> buscarPorIdEUsuario(Long id, Long usuarioId) { return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain); }
    public List<CartaoCredito> listarPorUsuario(Long usuarioId) { return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList(); }
    public Pagina<CartaoCredito> listarPorUsuario(Long usuarioId, Paginacao paginacao) { return PaginaJpaMapper.map(repository.findByUsuarioId(usuarioId, PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))), this::toDomain); }
    private CartaoCredito toDomain(CartaoCreditoJpaEntity entity) { return CartaoCredito.reconstituir(entity.getId(), entity.getUsuarioId(), entity.getNome(), ValorMonetario.of(entity.getLimite()), entity.getDiaFechamento(), entity.getDiaVencimento(), entity.isAtivo()); }
}
