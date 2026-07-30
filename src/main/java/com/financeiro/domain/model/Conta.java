package com.financeiro.domain.model;

import com.financeiro.domain.DomainException;
import com.financeiro.domain.vo.ValorMonetario;

public class Conta {

    private final Long id;
    private final Long usuarioId;
    private final String nome;
    private final TipoConta tipo;
    private final Long bancoId;
    private ValorMonetario saldo;
    private final boolean ativo;
    private final long version;

    private Conta(Long id, Long usuarioId, String nome, TipoConta tipo, Long bancoId,
                  ValorMonetario saldo, boolean ativo, long version) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.tipo = tipo;
        this.bancoId = bancoId;
        this.saldo = saldo;
        this.ativo = ativo;
        this.version = version;
        validarBanco();
    }

    public static Conta nova(Long usuarioId, String nome, TipoConta tipo, Long bancoId) {
        return new Conta(null, usuarioId, nome, tipo, bancoId, ValorMonetario.zero(), true, 0);
    }

    public static Conta reconstituir(Long id, Long usuarioId, String nome, TipoConta tipo,
                                     Long bancoId, ValorMonetario saldo, long version) {
        return new Conta(id, usuarioId, nome, tipo, bancoId, saldo, true, version);
    }

    public static Conta reconstituir(Long id, Long usuarioId, String nome, TipoConta tipo,
                                     Long bancoId, ValorMonetario saldo, boolean ativo, long version) {
        return new Conta(id, usuarioId, nome, tipo, bancoId, saldo, ativo, version);
    }

    private void validarBanco() {
        if (tipo == TipoConta.FISICO && bancoId != null) {
            throw new DomainException("error.conta.fisico.sem.banco");
        }
        if (tipo != TipoConta.FISICO && bancoId == null) {
            throw new DomainException("error.conta.banco.obrigatorio");
        }
    }

    public void creditar(ValorMonetario valor) {
        this.saldo = this.saldo.somar(valor);
    }

    public void debitar(ValorMonetario valor) {
        this.saldo = this.saldo.subtrair(valor);
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getNome() { return nome; }
    public TipoConta getTipo() { return tipo; }
    public Long getBancoId() { return bancoId; }
    public ValorMonetario getSaldo() { return saldo; }
    public boolean isAtivo() { return ativo; }
    public long getVersion() { return version; }
}
