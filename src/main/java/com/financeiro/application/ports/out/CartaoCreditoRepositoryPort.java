package com.financeiro.application.ports.out;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.CartaoCredito;
import java.util.List;
import java.util.Optional;
public interface CartaoCreditoRepositoryPort { CartaoCredito salvar(CartaoCredito cartao); Optional<CartaoCredito> buscarPorIdEUsuario(Long id, Long usuarioId); List<CartaoCredito> listarPorUsuario(Long usuarioId); Pagina<CartaoCredito> listarPorUsuario(Long usuarioId, Paginacao paginacao); }
