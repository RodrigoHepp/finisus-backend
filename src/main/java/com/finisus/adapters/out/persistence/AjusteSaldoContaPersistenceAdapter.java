package com.finisus.adapters.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

import com.finisus.adapters.out.persistence.entity.AjusteSaldoContaJpaEntity;
import com.finisus.adapters.out.persistence.repository.AjusteSaldoContaJpaRepository;
import com.finisus.application.ports.out.AjusteSaldoContaRepositoryPort;
import com.finisus.domain.model.AjusteSaldoConta;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

@Component
public class AjusteSaldoContaPersistenceAdapter implements AjusteSaldoContaRepositoryPort {
	private final AjusteSaldoContaJpaRepository repository;

	public AjusteSaldoContaPersistenceAdapter(AjusteSaldoContaJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public AjusteSaldoConta salvar(AjusteSaldoConta ajuste) {
		var entity = new AjusteSaldoContaJpaEntity();
		entity.setId(ajuste.id());
		entity.setUsuarioId(ajuste.usuarioId());
		entity.setContaId(ajuste.contaId());
		entity.setSaldoAnterior(ajuste.saldoAnterior());
		entity.setSaldoCalculadoAnterior(ajuste.saldoCalculadoAnterior());
		entity.setSaldoInformado(ajuste.saldoInformado());
		entity.setValorAjuste(ajuste.valorAjuste());
		entity.setMotivo(ajuste.motivo());
		entity.setDataAjuste(ajuste.dataAjuste());
		entity.setChaveIdempotencia(ajuste.chaveIdempotencia());
		entity.setHashRequisicao(ajuste.hashRequisicao());
		entity.setVersion(ajuste.version());
		return toDomain(repository.save(entity));
	}

	@Override
	public Optional<AjusteSaldoConta> buscarPorUsuarioEChave(Long usuarioId, String chaveIdempotencia) {
		return repository.findByUsuarioIdAndChaveIdempotencia(usuarioId, chaveIdempotencia).map(this::toDomain);
	}

	@Override
	public Pagina<AjusteSaldoConta> listarPorContaEUsuario(Long contaId, Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(repository.findByContaIdAndUsuarioId(contaId, usuarioId,
				PaginaJpaMapper.pageable(paginacao,
						Sort.by("dataAjuste").descending().and(Sort.by("id").descending()))), this::toDomain);
	}

	private AjusteSaldoConta toDomain(AjusteSaldoContaJpaEntity entity) {
		return new AjusteSaldoConta(entity.getId(), entity.getUsuarioId(), entity.getContaId(),
				entity.getSaldoAnterior(), entity.getSaldoCalculadoAnterior(), entity.getSaldoInformado(),
				entity.getValorAjuste(),
				entity.getMotivo(), entity.getDataAjuste(), entity.getChaveIdempotencia(), entity.getHashRequisicao(),
				entity.getVersion() == null ? 0 : entity.getVersion());
	}
}
