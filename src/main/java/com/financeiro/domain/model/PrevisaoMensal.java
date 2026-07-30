package com.financeiro.domain.model;

import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;

import java.time.LocalDateTime;

public class PrevisaoMensal {

    private final Long id;
    private final Long usuarioId;
    private final AnoMes anoMes;
    private final Long categoriaId;
    private final ValorMonetario valorProjetadoEntrada;
    private final ValorMonetario valorProjetadoSaida;
    private final LocalDateTime calculadoEm;

    private PrevisaoMensal(Long id, Long usuarioId, AnoMes anoMes, Long categoriaId,
                           ValorMonetario valorProjetadoEntrada, ValorMonetario valorProjetadoSaida,
                           LocalDateTime calculadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.anoMes = anoMes;
        this.categoriaId = categoriaId;
        this.valorProjetadoEntrada = valorProjetadoEntrada;
        this.valorProjetadoSaida = valorProjetadoSaida;
        this.calculadoEm = calculadoEm;
    }

    public static PrevisaoMensal calcular(Long usuarioId, AnoMes anoMes, Long categoriaId,
                                          ValorMonetario entrada, ValorMonetario saida, LocalDateTime calculadoEm) {
        return new PrevisaoMensal(null, usuarioId, anoMes, categoriaId, entrada, saida,
                calculadoEm);
    }

    public static PrevisaoMensal reconstituir(Long id, Long usuarioId, AnoMes anoMes,
                                              Long categoriaId, ValorMonetario entrada,
                                              ValorMonetario saida, LocalDateTime calculadoEm) {
        return new PrevisaoMensal(id, usuarioId, anoMes, categoriaId, entrada, saida, calculadoEm);
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public AnoMes getAnoMes() { return anoMes; }
    public Long getCategoriaId() { return categoriaId; }
    public ValorMonetario getValorProjetadoEntrada() { return valorProjetadoEntrada; }
    public ValorMonetario getValorProjetadoSaida() { return valorProjetadoSaida; }
    public LocalDateTime getCalculadoEm() { return calculadoEm; }
}
