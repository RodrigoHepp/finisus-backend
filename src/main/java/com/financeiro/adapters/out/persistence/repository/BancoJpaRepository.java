package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.BancoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BancoJpaRepository extends JpaRepository<BancoJpaEntity, Long> {

    List<BancoJpaEntity> findByUsuarioIdIsNullAndAtivoTrue();

    List<BancoJpaEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId);

    List<BancoJpaEntity> findByUsuarioIdIsNullOrUsuarioId(Long usuarioId);

    @Query("select b from BancoJpaEntity b where b.ativo = true and (b.usuarioId is null or b.usuarioId = :usuarioId)")
    Page<BancoJpaEntity> findDisponiveisParaUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);
}
