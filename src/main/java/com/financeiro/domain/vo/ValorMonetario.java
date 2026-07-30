package com.financeiro.domain.vo;

import com.financeiro.domain.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ValorMonetario(BigDecimal valor) {

    private static final int SCALE = 2;

    public ValorMonetario {
        if (valor == null) {
            throw new DomainException("error.valor.required");
        }
        valor = valor.setScale(SCALE, RoundingMode.HALF_UP);
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("error.valor.negative");
        }
    }

    public static ValorMonetario zero() {
        return new ValorMonetario(BigDecimal.ZERO);
    }

    public static ValorMonetario of(BigDecimal valor) {
        return new ValorMonetario(valor);
    }

    public ValorMonetario somar(ValorMonetario outro) {
        return new ValorMonetario(this.valor.add(outro.valor));
    }

    public ValorMonetario subtrair(ValorMonetario outro) {
        return new ValorMonetario(this.valor.subtract(outro.valor));
    }

    public boolean isZero() {
        return valor.compareTo(BigDecimal.ZERO) == 0;
    }
}
