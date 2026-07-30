package com.financeiro.application.ports.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fonte da data operacional, definida pela infraestrutura. */
public interface ObterDataAtualPort {
    LocalDate obter();
    LocalDateTime obterDataHora();
}
