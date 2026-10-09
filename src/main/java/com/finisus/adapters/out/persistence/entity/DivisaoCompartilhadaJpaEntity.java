package com.finisus.adapters.out.persistence.entity;

import com.finisus.domain.model.StatusDivisaoCompartilhada;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "divisao_compartilhada")
@Getter
@Setter
public class DivisaoCompartilhadaJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "criador_id", nullable = false)
    private Long criadorId;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDivisaoCompartilhada status;

    @Version
    @Column(nullable = false)
    private long version;

    @OneToMany(mappedBy = "divisao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipanteDivisaoJpaEntity> participantes = new ArrayList<>();
}
