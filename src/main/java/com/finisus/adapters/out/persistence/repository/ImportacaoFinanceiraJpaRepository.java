package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.ImportacaoFinanceiraJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImportacaoFinanceiraJpaRepository extends JpaRepository<ImportacaoFinanceiraJpaEntity, Long> {
	Optional<ImportacaoFinanceiraJpaEntity> findByUsuarioIdAndBancoIdAndHashArquivo(
			Long usuarioId, Long bancoId, String hashArquivo);

	Optional<ImportacaoFinanceiraJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from ImportacaoFinanceiraJpaEntity i left join fetch i.lancamentos "
			+ "where i.id = :id and i.usuarioId = :usuarioId")
	Optional<ImportacaoFinanceiraJpaEntity> findByIdAndUsuarioIdForUpdate(@Param("id") Long id,
			@Param("usuarioId") Long usuarioId);
}
