package com.financeiro.application.service;

import com.financeiro.application.ports.in.GerenciarPerfilUseCase;
import com.financeiro.application.ports.out.RefreshTokenRepositoryPort;
import com.financeiro.application.ports.out.UsuarioRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Usuario;
import com.financeiro.domain.vo.Email;

public class PerfilUsuarioService implements GerenciarPerfilUseCase {
    private final UsuarioRepositoryPort usuarios;
    private final RefreshTokenRepositoryPort refreshTokens;
    public PerfilUsuarioService(UsuarioRepositoryPort usuarios, RefreshTokenRepositoryPort refreshTokens) { this.usuarios = usuarios; this.refreshTokens = refreshTokens; }
    public Usuario consultar(Long usuarioId) { return buscar(usuarioId); }
    public Usuario atualizar(Long usuarioId, AtualizarCommand command) { Usuario atual = buscar(usuarioId); Email email = new Email(command.email()); if (!atual.getEmail().valor().equals(email.valor()) && usuarios.existePorEmail(email.valor())) throw new DomainException("error.usuario.email.existe"); return usuarios.salvar(Usuario.reconstituir(atual.getId(), command.nome(), email, atual.getSenhaHash(), atual.isAtivo(), atual.getCriadoEm(), atual.getSessaoVersao())); }
    public void desativar(Long usuarioId) { Usuario atual = buscar(usuarioId); usuarios.salvar(Usuario.reconstituir(atual.getId(), atual.getNome(), atual.getEmail(), atual.getSenhaHash(), false, atual.getCriadoEm(), atual.getSessaoVersao() + 1)); refreshTokens.invalidarTodosDoUsuario(usuarioId); }
    private Usuario buscar(Long id) { return usuarios.buscarPorId(id).orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")); }
}
