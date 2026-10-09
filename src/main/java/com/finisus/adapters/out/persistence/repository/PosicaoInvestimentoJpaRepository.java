package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.PosicaoInvestimentoJpaEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PosicaoInvestimentoJpaRepository extends JpaRepository<PosicaoInvestimentoJpaEntity, Long> {
	Page<PosicaoInvestimentoJpaEntity> findByInvestimentoId(Long investimentoId, Pageable pageable);

	@Query("""
			select p from PosicaoInvestimentoJpaEntity p
			where p.investimentoId = :investimentoId and p.dataReferencia <= :referencia
			order by p.dataReferencia desc, p.id desc
			""")
	Optional<PosicaoInvestimentoJpaEntity> findFirstByInvestimentoIdAndDataReferenciaLessThanEqualOrderByDataReferenciaDesc(
			@Param("investimentoId") Long investimentoId, @Param("referencia") LocalDate referencia);

	@Query("""
			select p from PosicaoInvestimentoJpaEntity p
			where p.investimentoId in :investimentosIds
			  and p.dataReferencia <= :referencia
			  and not exists (
				select 1 from PosicaoInvestimentoJpaEntity posterior
				where posterior.investimentoId = p.investimentoId
				  and posterior.dataReferencia <= :referencia
				  and (posterior.dataReferencia > p.dataReferencia
				       or (posterior.dataReferencia = p.dataReferencia and posterior.id > p.id))
			  )
			""")
	List<PosicaoInvestimentoJpaEntity> findUltimasAte(
			@Param("investimentosIds") List<Long> investimentosIds, @Param("referencia") LocalDate referencia);
}
