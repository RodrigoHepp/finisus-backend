package com.finisus.adapters.out.persistence;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.finisus.adapters.out.persistence.entity.PrevisaoMensalJpaEntity;
import com.finisus.adapters.out.persistence.repository.PrevisaoMensalJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.PrevisaoMensalRepositoryPort;
import com.finisus.domain.model.PrevisaoMensal;
import com.finisus.domain.vo.AnoMes;
import com.finisus.domain.vo.ValorMonetario;

@Component
public class PrevisaoMensalPersistenceAdapter implements PrevisaoMensalRepositoryPort {
	private final PrevisaoMensalJpaRepository repository;

	public PrevisaoMensalPersistenceAdapter(PrevisaoMensalJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public void substituir(Long usuarioId, AnoMes anoMes, List<PrevisaoMensal> previsoes) {
		repository.deleteByUsuarioIdAndAnoMes(usuarioId, anoMes.formatado());
		repository.saveAll(previsoes.stream().map(this::toEntity).toList());
	}

	@Override
	public List<PrevisaoMensal> listar(Long usuarioId, AnoMes anoMes) {
		return repository.findByUsuarioIdAndAnoMes(usuarioId, anoMes.formatado()).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<PrevisaoMensal> listar(Long usuarioId, AnoMes anoMes, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository
						.findByUsuarioIdAndAnoMes(usuarioId, anoMes.formatado(),
								PaginaJpaMapper.pageable(paginacao,
										Sort.by("categoriaId").ascending().and(Sort.by("id").ascending()))),
				this::toDomain);
	}

	private PrevisaoMensalJpaEntity toEntity(PrevisaoMensal p) {
		PrevisaoMensalJpaEntity e = new PrevisaoMensalJpaEntity();
		e.setUsuarioId(p.getUsuarioId());
		e.setAnoMes(p.getAnoMes().formatado());
		e.setCategoriaId(p.getCategoriaId());
		e.setValorProjetadoEntrada(p.getValorProjetadoEntrada().valor());
		e.setValorProjetadoSaida(p.getValorProjetadoSaida().valor());
		e.setCalculadoEm(p.getCalculadoEm());
		return e;
	}

	private PrevisaoMensal toDomain(PrevisaoMensalJpaEntity e) {
		return PrevisaoMensal.reconstituir(e.getId(), e.getUsuarioId(), AnoMes.parse(e.getAnoMes()), e.getCategoriaId(),
				ValorMonetario.of(e.getValorProjetadoEntrada()), ValorMonetario.of(e.getValorProjetadoSaida()),
				e.getCalculadoEm());
	}
}
