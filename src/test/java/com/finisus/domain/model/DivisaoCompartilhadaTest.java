package com.finisus.domain.model;

import com.finisus.domain.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class DivisaoCompartilhadaTest {
    @Test
    void exigeQueOCriadorParticipeEDivisaoSomeCemPorCento() {
        assertThatThrownBy(() -> DivisaoCompartilhada.nova(1L, "Luz",
                List.of(new ParticipanteDivisao(2L, new BigDecimal("50.00")),
                        new ParticipanteDivisao(3L, new BigDecimal("50.00")))))
                .isInstanceOf(DomainException.class)
                .hasMessage("error.divisao.participantes.invalidos");
    }

    @Test
    void permiteGrupoSemPercentualHistorico() {
        assertThatCode(() -> DivisaoCompartilhada.nova(1L, "Casa",
                List.of(new ParticipanteDivisao(1L, null), new ParticipanteDivisao(2L, null))))
                .doesNotThrowAnyException();
    }

    @Test
    void recusaMisturaDeParticipantesComESemPercentual() {
        assertThatThrownBy(() -> DivisaoCompartilhada.nova(1L, "Casa",
                List.of(new ParticipanteDivisao(1L, new BigDecimal("50")),
                        new ParticipanteDivisao(2L, null))))
                .isInstanceOf(DomainException.class)
                .hasMessage("error.divisao.participantes.invalidos");
    }
}
