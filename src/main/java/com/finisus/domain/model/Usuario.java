package com.finisus.domain.model;

import com.finisus.domain.vo.Email;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Usuario {
	private static final int LIMITE_TENTATIVAS_LOGIN_INVALIDAS = 5;

	private final Long id;
	private final String nome;
	private final Email email;
	private final String senhaHash;
	private final boolean ativo;
	private final LocalDateTime criadoEm;
	private final long sessaoVersao;
	private final int tentativasLoginInvalidas;
	private final boolean bloqueado;
	private final Set<PermissaoUsuario> permissoes;

	private Usuario(Long id, String nome, Email email, String senhaHash, boolean ativo, LocalDateTime criadoEm,
			long sessaoVersao, int tentativasLoginInvalidas, boolean bloqueado, Set<PermissaoUsuario> permissoes) {
		if (tentativasLoginInvalidas < 0) {
			throw new IllegalArgumentException("Tentativas de login invalidas nao podem ser negativas");
		}
		this.id = id;
		this.nome = nome;
		this.email = email;
		this.senhaHash = senhaHash;
		this.ativo = ativo;
		this.criadoEm = criadoEm;
		this.sessaoVersao = sessaoVersao;
		this.tentativasLoginInvalidas = tentativasLoginInvalidas;
		this.bloqueado = bloqueado;
		this.permissoes = permissoes == null || permissoes.isEmpty()
				? Collections.emptySet()
				: Collections.unmodifiableSet(EnumSet.copyOf(permissoes));
	}

	public static Usuario novo(String nome, Email email, String senhaHash, LocalDateTime criadoEm) {
		return novo(nome, email, senhaHash, criadoEm, Collections.emptySet());
	}

	public static Usuario novo(String nome, Email email, String senhaHash, LocalDateTime criadoEm,
			Set<PermissaoUsuario> permissoes) {
		return new Usuario(null, nome, email, senhaHash, true, criadoEm, 0, 0, false, permissoes);
	}

	public static Usuario reconstituir(Long id, String nome, Email email, String senhaHash, boolean ativo,
			LocalDateTime criadoEm, long sessaoVersao) {
		return reconstituir(id, nome, email, senhaHash, ativo, criadoEm, sessaoVersao, 0, false,
				Collections.emptySet());
	}

	public static Usuario reconstituir(Long id, String nome, Email email, String senhaHash, boolean ativo,
			LocalDateTime criadoEm, long sessaoVersao, int tentativasLoginInvalidas, boolean bloqueado,
			Set<PermissaoUsuario> permissoes) {
		return new Usuario(id, nome, email, senhaHash, ativo, criadoEm, sessaoVersao, tentativasLoginInvalidas,
				bloqueado, permissoes);
	}

	public Usuario registrarFalhaLogin() {
		if (bloqueado) {
			return this;
		}
		int tentativas = Math.min(tentativasLoginInvalidas + 1, LIMITE_TENTATIVAS_LOGIN_INVALIDAS);
		boolean deveBloquear = tentativas >= LIMITE_TENTATIVAS_LOGIN_INVALIDAS;
		return copiar(tentativas, deveBloquear, deveBloquear ? sessaoVersao + 1 : sessaoVersao);
	}

	public Usuario registrarLoginBemSucedido() {
		if (bloqueado || tentativasLoginInvalidas == 0) {
			return this;
		}
		return copiar(0, false, sessaoVersao);
	}

	public Usuario desbloquear() {
		return copiar(0, false, sessaoVersao + 1);
	}

	private Usuario copiar(int tentativas, boolean usuarioBloqueado, long novaSessaoVersao) {
		return new Usuario(id, nome, email, senhaHash, ativo, criadoEm, novaSessaoVersao, tentativas,
				usuarioBloqueado, permissoes);
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public Email getEmail() {
		return email;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public long getSessaoVersao() {
		return sessaoVersao;
	}

	public int getTentativasLoginInvalidas() {
		return tentativasLoginInvalidas;
	}

	public boolean isBloqueado() {
		return bloqueado;
	}

	public Set<PermissaoUsuario> getPermissoes() {
		return permissoes;
	}

	public boolean possuiPermissao(PermissaoUsuario permissao) {
		return permissoes.contains(permissao);
	}
}
