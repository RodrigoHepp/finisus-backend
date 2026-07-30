package com.financeiro.application.service;

import java.time.Duration;

public record ResultadoProcessamentoRecorrencias(int totalUsuarios, int sucessos, int falhas,
                                                 Duration duracao) {
}
