package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.finisus.domain.model.PermissaoUsuario;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class UsuarioJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nome;

	private String email;

	@Column(name = "senha_hash")
	private String senhaHash;

	private boolean ativo;

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	@Column(name = "sessao_versao", nullable = false)
	private long sessaoVersao;

	@Column(name = "tentativas_login_invalidas", nullable = false)
	private int tentativasLoginInvalidas;

	@Column(nullable = false)
	private boolean bloqueado;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "usuario_permissao", joinColumns = @JoinColumn(name = "usuario_id"))
	@Column(name = "permissao", nullable = false, length = 40)
	@Enumerated(EnumType.STRING)
	private Set<PermissaoUsuario> permissoes = new HashSet<>();
}
