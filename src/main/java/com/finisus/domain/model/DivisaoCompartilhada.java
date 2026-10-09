package com.finisus.domain.model;

import com.finisus.domain.DomainException;

import java.math.BigDecimal;
import java.util.List;

public class DivisaoCompartilhada {
    private final Long id;
    private final Long criadorId;
    private final String nome;
    private final List<ParticipanteDivisao> participantes;
    private StatusDivisaoCompartilhada status;

    private DivisaoCompartilhada(Long id, Long criadorId, String nome, List<ParticipanteDivisao> participantes,
            StatusDivisaoCompartilhada status) {
        if (criadorId == null || criadorId <= 0 || nome == null || nome.isBlank() || nome.length() > 120) {
            throw new DomainException("error.divisao.invalida");
        }
        validarParticipantes(criadorId, participantes);
        this.id = id;
        this.criadorId = criadorId;
        this.nome = nome.strip();
        this.participantes = List.copyOf(participantes);
        this.status = status == null ? StatusDivisaoCompartilhada.ATIVA : status;
    }

    public static DivisaoCompartilhada nova(Long criadorId, String nome, List<ParticipanteDivisao> participantes) {
        return new DivisaoCompartilhada(null, criadorId, nome, participantes, StatusDivisaoCompartilhada.ATIVA);
    }

    public static DivisaoCompartilhada reconstituir(Long id, Long criadorId, String nome,
            List<ParticipanteDivisao> participantes, StatusDivisaoCompartilhada status) {
        return new DivisaoCompartilhada(id, criadorId, nome, participantes, status);
    }

    public DivisaoCompartilhada comParticipantes(List<ParticipanteDivisao> novosParticipantes) {
        return new DivisaoCompartilhada(id, criadorId, nome, novosParticipantes, status);
    }

    public void inativar() {
        if (status != StatusDivisaoCompartilhada.ATIVA) {
            throw new DomainException("error.divisao.inativa");
        }
        status = StatusDivisaoCompartilhada.INATIVA;
    }

    public boolean possuiParticipante(Long usuarioId) {
        return participantes.stream().anyMatch(participante -> participante.usuarioId().equals(usuarioId));
    }

    public boolean isAtiva() {
        return status == StatusDivisaoCompartilhada.ATIVA;
    }

    public Long getId() { return id; }
    public Long getCriadorId() { return criadorId; }
    public String getNome() { return nome; }
    public List<ParticipanteDivisao> getParticipantes() { return participantes; }
    public StatusDivisaoCompartilhada getStatus() { return status; }

    private static void validarParticipantes(Long criadorId, List<ParticipanteDivisao> participantes) {
        if (participantes == null || participantes.isEmpty()
                || participantes.stream().map(ParticipanteDivisao::usuarioId).distinct().count() != participantes.size()
                || !participantes.stream().map(ParticipanteDivisao::usuarioId).anyMatch(criadorId::equals)) {
            throw new DomainException("error.divisao.participantes.invalidos");
        }
        long participantesComPercentual = participantes.stream().filter(p -> p.percentual() != null).count();
        if (participantesComPercentual != 0 && participantesComPercentual != participantes.size()) {
            throw new DomainException("error.divisao.participantes.invalidos");
        }
        if (participantesComPercentual > 0) {
            BigDecimal total = participantes.stream().map(ParticipanteDivisao::percentual).reduce(BigDecimal.ZERO,
                    BigDecimal::add);
            if (total.compareTo(BigDecimal.valueOf(100)) != 0) {
                throw new DomainException("error.divisao.participantes.invalidos");
            }
        }
    }
}
