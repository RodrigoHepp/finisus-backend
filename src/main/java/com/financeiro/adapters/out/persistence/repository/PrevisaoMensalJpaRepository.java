package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.PrevisaoMensalJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrevisaoMensalJpaRepository extends JpaRepository<PrevisaoMensalJpaEntity, Long> {
    List<PrevisaoMensalJpaEntity> findByUsuarioIdAndAnoMes(Long usuarioId, String anoMes);
    Page<PrevisaoMensalJpaEntity> findByUsuarioIdAndAnoMes(Long usuarioId, String anoMes, Pageable pageable);
    @Modifying @Query("DELETE FROM PrevisaoMensalJpaEntity p WHERE p.usuarioId = :usuarioId AND p.anoMes = :anoMes")
    void deleteByUsuarioIdAndAnoMes(@Param("usuarioId") Long usuarioId, @Param("anoMes") String anoMes);
}
