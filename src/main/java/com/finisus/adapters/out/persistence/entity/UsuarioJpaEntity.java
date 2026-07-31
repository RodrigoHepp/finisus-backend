package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
}
