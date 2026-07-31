package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.DespesaCompartilhadaJpaEntity;
import com.financeiro.adapters.out.persistence.entity.RateioDespesaJpaEntity;
import com.financeiro.adapters.out.persistence.repository.DespesaCompartilhadaJpaRepository;
import com.financeiro.adapters.out.persistence.repository.RateioDespesaJpaRepository;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.RateioDespesaRepositoryPort;
import com.financeiro.domain.model.RateioDespesa;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class RateioDespesaPersistenceAdapter implements RateioDespesaRepositoryPort {
	private final RateioDespesaJpaRepository repository;
	private final DespesaCompartilhadaJpaRepository despesas;

	public RateioDespesaPersistenceAdapter(RateioDespesaJpaRepository repository,
			DespesaCompartilhadaJpaRepository despesas) {
		this.repository = repository;
		this.despesas = despesas;
	}

	@Override
	public List<RateioDespesa> salvarTodos(Long despesaId, List<RateioDespesa> rateios) {
		DespesaCompartilhadaJpaEntity despesa = despesas.getReferenceById(despesaId);
		return rateios.stream().map(rateio -> repository.save(novaEntidade(despesa, rateio))).map(this::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<RateioDespesa> buscarPorId(Long rateioId) {
		return repository.findById(rateioId).map(this::toDomain);
	}

	@Override
	public RateioDespesa salvar(RateioDespesa rateio) {
		RateioDespesaJpaEntity entity = repository.findById(rateio.getId()).orElseThrow();
		entity.setStatus(rateio.getStatus());
		return toDomain(repository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<RateioDespesa> listarPorDespesaId(Long despesaId) {
		return repository.findByDespesaCompartilhadaId(despesaId).stream().map(this::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<RateioDespesa> listarPorDespesaId(Long despesaId, Paginacao paginacao) {
		return PaginaJpaMapper.map(repository.findByDespesaCompartilhadaId(despesaId,
				PaginaJpaMapper.pageable(paginacao, Sort.by("id").ascending())), this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<RateioDespesa> listarRecebidosPorUsuarioId(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository
						.findByUsuarioIdAndTipoParticipante(usuarioId,
								com.financeiro.domain.model.TipoParticipante.INTERNO, PaginaJpaMapper.pageable(
										paginacao, Sort.by("status").ascending().and(Sort.by("id").ascending()))),
				this::toDomain);
	}

	private RateioDespesaJpaEntity novaEntidade(DespesaCompartilhadaJpaEntity despesa, RateioDespesa rateio) {
		RateioDespesaJpaEntity entity = new RateioDespesaJpaEntity();
		entity.setDespesaCompartilhada(despesa);
		entity.setTipoParticipante(rateio.getTipoParticipante());
		entity.setUsuarioId(rateio.getUsuarioId());
		entity.setNomeExterno(rateio.getNomeExterno());
		entity.setEmailExterno(rateio.getEmailExterno());
		entity.setValorFixo(rateio.getValorFixo() == null ? null : rateio.getValorFixo().valor());
		entity.setPercentual(rateio.getPercentual());
		entity.setStatus(rateio.getStatus());
		return entity;
	}

	private RateioDespesa toDomain(RateioDespesaJpaEntity entity) {
		return RateioDespesa.reconstituir(entity.getId(), entity.getDespesaCompartilhada().getId(),
				entity.getTipoParticipante(), entity.getUsuarioId(), entity.getNomeExterno(), entity.getEmailExterno(),
				entity.getValorFixo() == null ? null : ValorMonetario.of(entity.getValorFixo()), entity.getPercentual(),
				entity.getStatus());
	}
}
