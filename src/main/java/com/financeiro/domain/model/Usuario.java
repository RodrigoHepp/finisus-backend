package com.financeiro.domain.model;

import com.financeiro.domain.vo.Email;

import java.time.LocalDateTime;

public class Usuario {

    private final Long id;
    private final String nome;
    private final Email email;
    private final String senhaHash;
    private final boolean ativo;
    private final LocalDateTime criadoEm;
    private final long sessaoVersao;

    private Usuario(Long id, String nome, Email email, String senhaHash, boolean ativo, LocalDateTime criadoEm,
                    long sessaoVersao) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.sessaoVersao = sessaoVersao;
    }

    public static Usuario novo(String nome, Email email, String senhaHash, LocalDateTime criadoEm) {
        return new Usuario(null, nome, email, senhaHash, true, criadoEm, 0);
    }

    public static Usuario reconstituir(Long id, String nome, Email email, String senhaHash,
                                      boolean ativo, LocalDateTime criadoEm, long sessaoVersao) {
        return new Usuario(id, nome, email, senhaHash, ativo, criadoEm, sessaoVersao);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Email getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public long getSessaoVersao() { return sessaoVersao; }
}
